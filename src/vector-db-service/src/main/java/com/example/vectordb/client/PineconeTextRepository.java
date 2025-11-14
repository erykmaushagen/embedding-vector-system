package com.example.vectordb.client;

import java.util.List;

import com.example.vectordb.client.*;
import com.example.vectordb.client.Model.*;

import io.pinecone.clients.Pinecone;
import com.example.vectordb.client.Model.*;

/* 
 * ➡️ Ein Index für ein KnowledgeItems
 * ⬆️  Embeddings unterschiedlich dimensioniert sind (was sehr wahrscheinlich ist)
 */

public class PineconeTextRepository extends KnowledgeRepository<TextKnowledge> {
    private final String namespace = "text";
    private final String indexName = "text-embeddings";

    public PineconeTextRepository(Pinecone client) {
        super(client);
    }

    @Override
    public void save(TextKnowledge item ) {
        
    }

    public TextKnowledge findById(String id){

        return null; 
    }

    public List<TextKnowledge> searchSimiliar(float[] queryEmbeddding, int topK) {
        return null; 
    }

    public void delete(String id){

    }
}