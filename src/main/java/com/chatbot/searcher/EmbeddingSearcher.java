package com.chatbot.searcher;

import com.chatbot.service.QdrantHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmbeddingSearcher {


    private static final String EMBEDDED_CHUNKS_PATH = "src/main/resources/JsonEmbeddedChunks/";
    private final List<EmbeddedChunk> chunks;
    private QdrantHelper qdrantHelper = new QdrantHelper("http://localhost:6333");

    public EmbeddingSearcher(@Value("kratos") String jsonFileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.chunks = mapper.readValue(new File(EMBEDDED_CHUNKS_PATH+jsonFileName+".json"), new TypeReference<List<EmbeddedChunk>>() {});
    }

    public List<String> search(String queryEmbeddingAsCSV, int topK) {
        List<Double> queryVector = parseVector(queryEmbeddingAsCSV);
        PriorityQueue<ScoredChunk> topChunks = new PriorityQueue<>(Comparator.comparingDouble(c -> c.score));

        for (EmbeddedChunk chunk : chunks) {
            double score = cosineSimilarity(chunk.embedding, queryVector);
            if (topChunks.size() < topK) {
                topChunks.offer(new ScoredChunk(chunk.text, score));
            } else if (score > topChunks.peek().score) {
                topChunks.poll();
                topChunks.offer(new ScoredChunk(chunk.text, score));
            }
        }

        List<ScoredChunk> results = new ArrayList<>(topChunks);
        results.sort((a, b) -> Double.compare(b.score, a.score));
        return results.stream().map(c -> c.text).collect(Collectors.toList());
    }


    public String searchInQdrantDB(List<Double> queryVector, int topK, String collectionName) throws Exception {
        return qdrantHelper.search(collectionName, queryVector, topK, new HashMap<>());
    }


    private List<Double> parseVector(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1.size() != v2.size()) throw new IllegalArgumentException("Vectors must be of same length");
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            norm1 += Math.pow(v1.get(i), 2);
            norm2 += Math.pow(v2.get(i), 2);
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public static class EmbeddedChunk {
        public String chunkId;
        public String text;
        public List<Double> embedding;
        public int position;
        public String source;
    }

    private static class ScoredChunk {
        public String text;
        public double score;

        public ScoredChunk(String text, double score) {
            this.text = text;
            this.score = score;
        }
    }
}
