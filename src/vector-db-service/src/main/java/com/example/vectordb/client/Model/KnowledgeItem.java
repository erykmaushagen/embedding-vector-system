package com.example.vectordb.client.Model;

import java.util.*; 

public interface KnowledgeItem {
    
    public String getId();

    float[] getEmbedding();
}
