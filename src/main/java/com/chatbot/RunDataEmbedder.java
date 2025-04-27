package com.chatbot;

import com.chatbot.indexer.TextBookIndexer;
import com.chatbot.service.ChunkEmbedder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

public class RunDataEmbedder {
    public static void main(String[] args) throws Exception {

        // Index the text file into chunks.txt
        System.out.println("\nStarting data indexing...");
        String filePath = "src/main/resources/RawData/laravel-11.x.txt";
        String jsonFileName = "laravel-11";

        System.out.println("\nLoading rawData from " + filePath);

        TextBookIndexer.indexTextbook(filePath);

        System.out.println("\nThe chunks are saved into text file 'chunks.txt' for emebedding ");



        System.out.println("\nStarting to data embedder data from generated chunks \n\n");
        ChunkEmbedder embedder = new ChunkEmbedder();


        embedder.embedChunks(jsonFileName);

        System.out.println("\nFinished data embedder data from generated chunks into " + jsonFileName +".json");

    }

    @Configuration
    @ComponentScan(basePackages = "com.chatbot") // adjust if needed
    @PropertySource("classpath:application.properties")
    public static class AppConfig {}
}