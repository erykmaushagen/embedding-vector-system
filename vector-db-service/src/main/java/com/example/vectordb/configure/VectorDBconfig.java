package com.example.vectordb.configure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pinecone.clients.Pinecone;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

class VectorDBconfig {
    private String meinKey;
    private ObjectMapper mapper;

    private void ensureKeyloaded() {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        if (meinKey == null) {
            Path path = Path.of("secrets.json");
            if (Files.exists(path)) {
                try {

                    String json = Files.readString(path, StandardCharsets.UTF_8);
                    JsonNode root = mapper.readTree(json);
                    String key = root.path("key").asText(null);
                    if (key != null && !key.isBlank()) {
                        meinKey = key;
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

    public Pinecone getVDBinstance() {
        ensureKeyloaded();
        Pinecone instance = new Pinecone.Builder(meinKey).build();
        return instance;
    }
}