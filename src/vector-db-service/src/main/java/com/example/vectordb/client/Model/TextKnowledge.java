package com.example.vectordb.client.Model;


import java.util.*;


public class TextKnowledge implements KnowledgeItem {
    private final String id;
    private final String text;
    private final float[] embedding;


    public TextKnowledge() {
        id = null;
        text = null;
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
