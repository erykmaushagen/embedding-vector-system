package com.example.vectordb.client.Model;

import java.util.*;


public class ImageKnowledge implements KnowledgeItem {
    private final String id;
    private final byte[] imageData;
    private final float[] embedding;

    public ImageKnowledge(){
        id = null; 
        imageData = null;
        embedding = null;
    }


    
    @Override 
    public String getId() {
        return null;
    }


    @Override
    public float[] getEmbedding() {

        return null;
    }
}
