package com.chatbot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JsonToQdrantImporter {

    private final QdrantHelper qdrantHelper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public JsonToQdrantImporter(QdrantHelper qdrantHelper) {
        this.qdrantHelper = qdrantHelper;
    }

    public void importJson(String jsonFilePath, String collectionName) throws Exception {
        List<EmbeddedChunk> chunks = objectMapper.readValue(
                new File(jsonFilePath),
                new TypeReference<List<EmbeddedChunk>>() {}
        );

        System.out.println("📥 Loaded " + chunks.size() + " chunks from JSON");

        int batchSize = 50;

        for (int i = 0; i < chunks.size(); i += batchSize) {
            List<EmbeddedChunk> batch = chunks.subList(i, Math.min(i + batchSize, chunks.size()));

            List<String> ids = batch.stream().map(c -> c.chunkId).collect(Collectors.toList());
            List<List<Double>> vectors = batch.stream().map(c -> c.embedding).collect(Collectors.toList());
            List<Map<String, Object>> payloads = batch.stream().map(c -> {
                Map<String, Object> map = new HashMap<>();
                map.put("text", c.text);
                map.put("source", c.source);
                map.put("position", c.position);
                return map;
            }).collect(Collectors.toList());

            qdrantHelper.addBatchOfEmbeddings(collectionName, ids, vectors, payloads);
        }

        System.out.println("✅ All chunks imported to Qdrant collection: " + collectionName);
    }

    public static class EmbeddedChunk {
        public String chunkId;
        public String text;
        public List<Double> embedding;
        public int position;
        public String source;

        public EmbeddedChunk() {}
    }
}
