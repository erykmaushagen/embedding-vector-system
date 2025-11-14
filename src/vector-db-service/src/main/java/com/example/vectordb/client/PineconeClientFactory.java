package com.example.vectordb.client;

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
 * Hilfsklasse zur Konfiguration des HTTP-CLients der Pinecone Vector Database
 * Clients
 */
class HTTPConfig {
    public int connectionTimeout = 30;
    public int readTimeout = 60;
    public int writeTimeout = 60;
    public boolean retryOnConnectionFailure = true;

}

/**
 * Konfigurationsklasse für Pinecone Vector Database Client.
 * Thread-safe Singleton-Pattern mit lazy initialization.
 */
public class PineconeClientFactory {

    private String apiKey;
    private PineconeConfig config;
    private Pinecone client;
    private ObjectMapper mapper;
    private final Object lock = new Object();
    private HTTPConfig httpConfigData;

    /**
     * Konstruktor lädt API-Key aus JSON
     */
    public PineconeClientFactory() {
        ensureKeyloaded(); //
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API-Key darf nicht null oder leer sein");
        }
        httpConfigData = new HTTPConfig();
        this.config = buildConfig();
    }

    /**
     * Konstruktor mit vollständiger Konfiguration
     * 
     * @param apiKey Pinecone API-Key (erforderlich)
     */
    public PineconeClientFactory(String apiKey, PineconeConfig customConfig) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API-Key darf nicht null oder leer sein");
        }
        this.apiKey = apiKey;
        httpConfigData = new HTTPConfig();
        this.config = customConfig != null ? customConfig : buildConfig();
    }

    /**
     * Erstellt Standard-Konfiguration basierend auf Pinecone SDK Best Practices
     */
    private PineconeConfig buildConfig() {

        PineconeConfig cfg = new PineconeConfig(apiKey);

        // Source Tag für Tracking/Analytics
        cfg.setSourceTag("embedding-vector-service");

        OkHttpClient customHttpClient = new OkHttpClient.Builder()
                .connectTimeout(httpConfigData.connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(httpConfigData.readTimeout, TimeUnit.SECONDS)
                .writeTimeout(httpConfigData.writeTimeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(httpConfigData.retryOnConnectionFailure)
                .build();

        cfg.setCustomOkHttpClient(customHttpClient);

        cfg.setTLSEnabled(true);

        return cfg;
    }

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
            System.err.println("Fehler beim Lesen oder Parsen von secrets.json: " + e.getMessage());
        }
        System.out.println(apiKey);

    }

    /**
     * erhalte Pinecone Client auf Basis dessen Konfiguration
     */
    public Pinecone getClient() {
        if (client == null) {
            synchronized (lock) {
                if (client == null) {
                    try {
                        config.validate();

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

    /**
     * Gibt die aktuelle Konfiguration zurück (Read-Only)
     */
    public PineconeConfig getConfig() {
        return config;
    }

    /**
     * Setzt die Verbindungs-Timeout in Millisekunden
     * 
     * @param time
     */
    public void setConnectionTimeOut(int time) {
        httpConfigData.connectionTimeout = time;
    }

    public int getConnetionTimeOut() {
        return httpConfigData.connectionTimeout;
    }

    /**
     * Setzt die Lese-Timeout in Millisekunden
     * 
     * @param time
     */
    public void setReadTimeOut(int time) {
        httpConfigData.readTimeout = time;
    }

    public int getReadTimeOut() {
        return httpConfigData.readTimeout;
    }

    /**
     * Setzt die Schreib-Timeout in Millisekunden
     * 
     * @param time
     */
    public void setWriteTimeOut(int time) {
        httpConfigData.writeTimeout = time;
    }

    public int getWriteTimeOut() {
        return httpConfigData.writeTimeout;
    }

    /**
     * Setzt ob bei Verbindungsfehlern neu versucht werden soll
     * 
     * @param retry
     */
    public void setRetryOnConnectionFailure(boolean retry) {
        httpConfigData.retryOnConnectionFailure = retry;
    }

    public boolean isRetryOnConnectionFailureOn() {
        return httpConfigData.retryOnConnectionFailure;
    }
}