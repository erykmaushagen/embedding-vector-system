package com.example.vectordb.configure;

import io.pinecone.clients.Pinecone;
import io.pinecone.configs.PineconeConfig;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(VectorDataBaseConfig.class);
    
    private String apiKey;
    private final PineconeConfig config;
    private volatile Pinecone client;
    private ObjectMapper mapper;
    private final Object lock = new Object();
    
    /**
     * Konstruktor mit API-Key
     * @param apiKey Pinecone API-Key (erforderlich)
     */
    public VectorDataBaseConfig() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API-Key darf nicht null oder leer sein");
        }
        this.config = buildConfig();
    }
    
    /**
     * Konstruktor mit vollständiger Konfiguration
     */
    public VectorDataBaseConfig(String apiKey, PineconeConfig customConfig) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            ensureKeyloaded();
            throw new IllegalArgumentException("API-Key darf nicht null oder leer sein");
        }
        this.apiKey = apiKey;
        this.config = customConfig != null ? customConfig : buildConfig();
    }
    
    /**
     * Erstellt Standard-Konfiguration basierend auf Pinecone SDK Best Practices
     */
    private PineconeConfig buildConfig() {
        ensureKeyloaded();
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

    private void ensureKeyloaded() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        if (apiKey == null) {
            Path path = Path.of("secrets.json");
            if (Files.exists(path)) {
                try {

                    String json = Files.readString(path, StandardCharsets.UTF_8);
                    JsonNode root = mapper.readTree(json);
                    String key = root.path("key").asText(null);
                    if (key != null && !key.isBlank()) {
                        apiKey = key;
                        return;
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(
                            "Error reading or parsing 'secrets.json'. Please check file validity and permissions.", e);
                }
            }
        }
        throw new IllegalStateException(
                "Pinecone-API-Key not found (or secrets.json not given or missing 'key' field)");
    }


    /**
     * Gibt Pinecone Client Instanz zurück (Singleton mit lazy initialization)
     * Thread-safe implementation mit Double-Checked Locking
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
                        
                        log.info("Pinecone Client erfolgreich initialisiert");
                    } catch (Exception e) {
                        log.error("Fehler bei der Initialisierung des Pinecone Clients", e);
                        throw new RuntimeException("Pinecone Client konnte nicht erstellt werden", e);
                    }
                }
            }
        }
        return client;
    }
    
    /**
     * Testet ob die Verbindung zum Pinecone Service funktioniert
     */
    public boolean testConnection() {
        try {
            Pinecone testClient = getClient();
            // Versuche Indexes aufzulisten als Connection-Test
            testClient.listIndexes();
            log.info("Verbindungstest erfolgreich");
            return true;
        } catch (Exception e) {
            log.error("Verbindungstest fehlgeschlagen", e);
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
    public void shutdown() {
        if (client != null) {
            synchronized (lock) {
                if (client != null) {
                    try {
                        // Cleanup falls notwendig
                        client = null;
                        log.info("Pinecone Client heruntergefahren");
                    } catch (Exception e) {
                        log.error("Fehler beim Herunterfahren", e);
                    }
                }
            }
        }
    }
}