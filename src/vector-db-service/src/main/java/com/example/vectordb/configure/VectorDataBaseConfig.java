package com.example.vectordb.configure;

import io.pinecone.clients.Pinecone;
import io.pinecone.configs.PineconeConfig;
import okhttp3.OkHttpClient;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Konfigurationsklasse für Pinecone Vector Database Client.
 * Thread-safe Singleton-Pattern mit lazy initialization.
 */
public class VectorDataBaseConfig {

    private String apiKey;
    private PineconeConfig config;
    private Pinecone client;
    private ObjectMapper mapper;
    private final Object lock = new Object();

    /**
     * Konstruktor lädt API-Key aus JSON
     */
    public VectorDataBaseConfig() {
        ensureKeyloaded(); //
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API-Key darf nicht null oder leer sein");
        }
        this.config = buildConfig();
    }

    /**
     * Konstruktor mit vollständiger Konfiguration
     * 
     * @param apiKey Pinecone API-Key (erforderlich)
     */
    public VectorDataBaseConfig(String apiKey, PineconeConfig customConfig) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API-Key darf nicht null oder leer sein");
        }
        this.apiKey = apiKey;
        this.config = customConfig != null ? customConfig : buildConfig();
    }

    /**
     * Erstellt Standard-Konfiguration basierend auf Pinecone SDK Best Practices
     */
    private PineconeConfig buildConfig() {

        PineconeConfig cfg = new PineconeConfig(apiKey);

        // Source Tag für Tracking/Analytics
        cfg.setSourceTag("embedding-vector-service");

        // Custom HTTP Client mit Timeouts
        OkHttpClient customHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        cfg.setCustomOkHttpClient(customHttpClient);

        // TLS sollte normalerweise aktiviert sein für Production
        cfg.setTLSEnabled(true);

        return cfg;
    }
    // apiKey, sourceTag, proxyConfig, customOkHttpClient

    /**
     * Überprüft korrektes Laden der .json mit dem API-Key
     */
    private void ensureKeyloaded() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        // Frühzeitiger Exit, wenn bereits gesetzt
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return;
        }

        Path path = Path.of("secrets.json");

        try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("secrets.json")) {

            if (is == null) {
                // Die Datei wurde nicht auf dem Classpath gefunden.
                throw new IllegalStateException(
                        "'secrets.json' not found on the classpath. Please ensure it's in src/main/resources.");
            }

            System.out.println("Lade API-Key als Classpath-Ressource.");

            String json = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);

            JsonNode root = mapper.readTree(json);
            String key = root.path("key").asText(null);

            if (key != null && !key.isBlank()) {
                apiKey = key;
                return;
            }
        } catch (IOException e) {
            // Hier fangen Sie die Ausnahme ab
            System.err.println("Fehler beim Lesen oder Parsen von secrets.json: " + e.getMessage());
        }
        System.out.println(apiKey);

    }

    /**
     * erhalte Pinecone Client aus der Konfiguration
     */
    public Pinecone getClient() {
        if (client == null) {
            synchronized (lock) {
                if (client == null) {
                    try {
                        // Config validieren
                        config.validate();

                        // Client mit Konfiguration erstellen

                        // Client MUSS über den Builder erstellt werden.

                        Pinecone.Builder builder = new Pinecone.Builder(config.getApiKey());

                        if (config.getCustomOkHttpClient() != null) {
                            builder.withOkHttpClient(config.getCustomOkHttpClient());
                        }
                        if (config.getSourceTag() != null) {
                            builder.withSourceTag(config.getSourceTag());
                        }

                        if (config.getHost() != null) {
                            builder.withHost(config.getHost());
                        }

                        builder.withTlsEnabled(config.isTLSEnabled());

                        client = builder.build();

                        System.out.println("Pinecone Client erfolgreich initialisiert");
                    } catch (Exception e) {
                        System.out.println("Fehler bei der Initialisierung des Pinecone Clients" + e.getMessage());
                        throw new RuntimeException("Pinecone Client konnte nicht erstellt werden" + e.getMessage());
                    }
                }
            }
        }
        return client;
    }

    /**
     * Testet ob die Verbindung zum Pinecone Service funktioniert
     */
    public boolean testConnection(Pinecone clientToTest) {
        try {
            clientToTest.listIndexes();
            return true;
        } catch (Exception e) {
            System.out.println("Verbindungstest fehlgeschlagen: " + e.getMessage());
            return false;
        }

    }

    /**
     * Gibt die aktuelle Konfiguration zurück (Read-Only)
     */
    public PineconeConfig getConfig() {
        return config;
    }

    /**
     * Schließt den Client und gibt Ressourcen frei
     */
    public void shutdown(Pinecone clientToTest) {
        if (clientToTest != null) {
            synchronized (lock) {
                if (clientToTest != null) {
                    try {
                        // Cleanup falls notwendig
                        clientToTest = null;
                        System.out.println("Pinecone Client heruntergefahren");
                    } catch (Exception e) {
                        System.out.println("Fehler beim Herunterfahren" + e.getMessage());
                    }
                }
            }
        }
    }
}