package com.chatbot;

import com.chatbot.service.JsonToQdrantImporter;
import com.chatbot.service.QdrantHelper;

public class RunJsonQdrandImporter {

    public static void main(String[] args) throws Exception {
        QdrantHelper qdrantHelper = new QdrantHelper("http://localhost:6333");
        qdrantHelper.createCollection("laravel_embeddings");

        JsonToQdrantImporter jsonToQdrantImporter = new JsonToQdrantImporter(qdrantHelper);

//        qdrantHelper.createCollection("laravel-11-embeddings");
        jsonToQdrantImporter.importJson("src/main/resources/JsonEmbeddedChunks/laravel-11.json", "laravel_embeddings");

    }
}

