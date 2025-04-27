package com.chatbot.service;

import com.chatbot.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
public class ChunkEmbedder {

    private final OpenAiEmbeddingModel embeddingModel;

    private static final String CHUNK_FILE_PATH = "src/main/resources/kratos_chunks.txt";
    private static String OUTPUT_FILE_PATH = "src/main/resources/JsonEmbeddedChunks/";
    private static String API_KEY = AppProperties.get("spring.ai.openai.api-key");

    private static QdrantHelper qdrantHelper = new QdrantHelper("http://localhost:6333");

    public ChunkEmbedder() {
        OpenAiApi openAiApi = new OpenAiApi(API_KEY);
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model("text-embedding-3-small")
                .build();
        System.out.println("✅ Embedding model set to: " + options.getModel());
        this.embeddingModel = new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }

    public void embedChunksToQdrant(String collectionName) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(CHUNK_FILE_PATH));
        List<String> validLines = lines.stream()
                .map(line -> line.replaceAll("\\s+", " ").trim())
                .filter(line -> line.length() >= 30)
                .toList();

        System.out.println("📦 Total valid chunks for Qdrant: " + validLines.size());
        long startTime = System.currentTimeMillis();

        List<String> idsBatch = new ArrayList<>();
        List<List<Double>> vectorsBatch = new ArrayList<>();
        List<Map<String, Object>> payloadsBatch = new ArrayList<>();

        int position = 0;
        int batchSize = 50; // 🚀 Batch size 50
        int totalInserted = 0;

        for (String cleaned : validLines) {
            EmbeddingRequest request = new EmbeddingRequest(List.of(cleaned), null);
            EmbeddingResponse response = embeddingModel.call(request);

            if (!response.getResults().isEmpty()) {
                float[] output = response.getResults().get(0).getOutput();
                List<Double> vector = new ArrayList<>();
                for (float v : output) {
                    vector.add((double) v);
                }

                String id = UUID.randomUUID().toString();
                idsBatch.add(id);
                vectorsBatch.add(vector);

                Map<String, Object> payload = new HashMap<>();
                payload.put("text", cleaned);
                payload.put("position", position++);
                payload.put("source", "textbook");
                payloadsBatch.add(payload);
            }

            // 🚀 When batch is full or last chunk
            if (idsBatch.size() == batchSize || position == validLines.size()) {
                qdrantHelper.addBatchOfEmbeddings(collectionName, idsBatch, vectorsBatch, payloadsBatch);
                totalInserted += idsBatch.size();

                System.out.printf("📦 Inserted batch, total inserted: %d / %d%n", totalInserted, validLines.size());

                // Reset batch
                idsBatch.clear();
                vectorsBatch.clear();
                payloadsBatch.clear();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("✅ All chunks embedded and stored in Qdrant collection '%s' in %.2f seconds.%n", collectionName, (endTime - startTime) / 1000.0);
    }

    public void embedChunks(String jsonFileName) throws IOException {
        OUTPUT_FILE_PATH = OUTPUT_FILE_PATH + jsonFileName + ".json";
        List<String> lines = Files.readAllLines(Paths.get(CHUNK_FILE_PATH));
        List<EmbeddedChunk> embeddedChunks = new ArrayList<>();
        int position = 0;

        // Filter valid chunks (length >= 30)
        List<String> validLines = lines.stream()
                .map(line -> line.replaceAll("\\s+", " ").trim())
                .filter(line -> line.length() >= 30)
                .toList();

        System.out.println("📦 Total valid chunks: " + validLines.size());
        long startTime = System.currentTimeMillis();

        for (String cleaned : validLines) {
            long chunkStart = System.currentTimeMillis();

            EmbeddingRequest request = new EmbeddingRequest(List.of(cleaned), null);
            EmbeddingResponse response = embeddingModel.call(request);

            if (!response.getResults().isEmpty()) {
                float[] output = response.getResults().get(0).getOutput();
                List<Double> vector = new ArrayList<>();
                for (float v : output) {
                    vector.add((double) v);
                }
                EmbeddedChunk chunk = new EmbeddedChunk(UUID.randomUUID().toString(), cleaned, vector, position++, "textbook");
                embeddedChunks.add(chunk);
            }

            // Progress log every 10 chunks
            if (position % 10 == 0 || position == validLines.size()) {
                System.out.printf("🧠 Embedded %d / %d chunks (%.2f%%)%n", position, validLines.size(), (100.0 * position / validLines.size()));
            }

            // Optional: print time taken per chunk
            // System.out.println("⏱️ Chunk took: " + (System.currentTimeMillis() - chunkStart) + "ms");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(new FileWriter(OUTPUT_FILE_PATH), embeddedChunks);

        long endTime = System.currentTimeMillis();
        System.out.printf("✅ Embedded chunks written to %s in %.2f seconds.%n", OUTPUT_FILE_PATH, (endTime - startTime) / 1000.0);
    }

    public static class EmbeddedChunk {
        public String chunkId;
        public String text;
        public List<Double> embedding;
        public int position;
        public String source;

        public EmbeddedChunk() {}

        public EmbeddedChunk(String chunkId, String text, List<Double> embedding, int position, String source) {
            this.chunkId = chunkId;
            this.text = text;
            this.embedding = embedding;
            this.position = position;
            this.source = source;
        }


    }
}