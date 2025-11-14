package com.example.vectordb.client;
/*
 *  Interface to persist data in the vector db
 *  - capsulates knowledege
 */
import java.util.*;

import com.example.vectordb.client.Model.*;

import io.pinecone.clients.Pinecone;
import io.pinecone.clients.Pinecone;
import sun.tools.jconsole.Tab;



public abstract class KnowledgeRepository <T extends  KnowledgeItem> {

    private Pinecone dbClient; 

    public KnowledgeRepository(Pinecone dbClient){
        this.dbClient = dbClient;
    }

    public abstract void save(T item);

    public abstract T findById(String id);

    public abstract List<T> searchSimiliar(float[] queryEmbeddding, int topK);

    public abstract void delete(String id);
    
}
