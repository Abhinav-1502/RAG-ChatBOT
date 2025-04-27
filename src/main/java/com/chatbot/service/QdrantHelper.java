package com.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class QdrantHelper {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public QdrantHelper(@Value("${qdrant.base-url:http://localhost:6333}") String baseUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.baseUrl = baseUrl;
    }

    public void createCollection(String collectionName) throws Exception {
        ObjectNode vectors = mapper.createObjectNode();
        vectors.put("size", 1536);
        vectors.put("distance", "Cosine");

        ObjectNode body = mapper.createObjectNode();
        body.set("vectors", vectors);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collections/" + collectionName))
                .PUT(HttpRequest.BodyPublishers.ofString(body.toString()))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("📂 Collection created: " + response.body());
    }

    public void addOneEmbedding(String collectionName, String id, List<Double> embedding, Map<String, Object> payload) throws Exception {
        addBatchOfEmbeddings(collectionName, List.of(id), List.of(embedding), List.of(payload));
    }

    public void addBatchOfEmbeddings(String collectionName, List<String> ids, List<List<Double>> vectors, List<Map<String, Object>> payloads) throws Exception {
        ObjectNode root = mapper.createObjectNode();
        ArrayNode points = mapper.createArrayNode();

        for (int i = 0; i < ids.size(); i++) {
            ObjectNode point = mapper.createObjectNode();
            point.put("id", ids.get(i));

            ArrayNode vector = mapper.createArrayNode();
            vectors.get(i).forEach(vector::add);
            point.set("vector", vector);

            ObjectNode payloadNode = mapper.convertValue(payloads.get(i), ObjectNode.class);
            point.set("payload", payloadNode);

            points.add(point);
        }

        root.set("points", points);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collections/" + collectionName + "/points?wait=true"))
                .PUT(HttpRequest.BodyPublishers.ofString(root.toString()))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("📦 Batch inserted: " + response.body());
    }

    public String search(String collectionName, List<Double> queryVector, int topK, Map<String, Object> filter) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        ArrayNode vector = mapper.createArrayNode();
        queryVector.forEach(vector::add);
        body.set("vector", vector);
        body.put("top", topK);
        body.put("with_payload", true);

        if (filter != null && !filter.isEmpty()) {
            ObjectNode filterNode = mapper.createObjectNode();
            ArrayNode mustArray = mapper.createArrayNode();
            for (Map.Entry<String, Object> entry : filter.entrySet()) {
                ObjectNode match = mapper.createObjectNode();
                match.putObject("match").put("value", entry.getValue().toString());
                ObjectNode condition = mapper.createObjectNode();
                condition.put("key", entry.getKey());
                condition.set("match", match.get("match"));
                mustArray.add(condition);
            }
            filterNode.set("must", mustArray);
            body.set("filter", filterNode);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collections/" + collectionName + "/points/search"))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
//        System.out.println("🔍 Search results: " + response.body().);
    }

    public static List<String> extractTextsFromQdrantResponse(String responseBody) throws Exception {
        List<String> texts = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(responseBody);
        JsonNode results = root.path("result");

        if (results.isArray()) {
            for (JsonNode result : results) {
                JsonNode textNode = result.path("payload").path("text");
                if (!textNode.isMissingNode()) {
                    texts.add(textNode.asText());
                }
            }
        }

        return texts;
    }
}

