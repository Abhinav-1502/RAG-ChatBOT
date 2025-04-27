package com.chatbot;

import com.chatbot.searcher.EmbeddingSearcher;
import com.chatbot.service.ChunkEmbedder;
import com.chatbot.service.EmbeddingService;
import com.chatbot.service.QdrantHelper;

import java.util.List;

public class TestRuns {

    public static void main(String[] args) throws Exception {


        // Initialize qdranthelper
        QdrantHelper qdrantHelper = new QdrantHelper("http://localhost:6333");

        // Create a new Collection
        qdrantHelper.createCollection("kratos_embeddings");


        // Initialize ChunkEmbedder
        ChunkEmbedder chunkEmbedder = new ChunkEmbedder();

        chunkEmbedder.embedChunksToQdrant("kratos_embeddings");



    }
}