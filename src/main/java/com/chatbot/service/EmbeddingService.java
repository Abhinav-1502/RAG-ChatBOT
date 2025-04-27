package com.chatbot.service;

import com.chatbot.config.AppProperties;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Basically converts the user query into searchable vectors
 * Service class responsible for generating text embeddings using OpenAI's Embedding API.
 * This class wraps the logic to call OpenAI embedding models and return output vectors.
 */
@Service
public class EmbeddingService {

    private final OpenAiEmbeddingModel embeddingModel;
    private String apiKey = AppProperties.get("spring.ai.openai.api-key");

    /**
            * Constructor that initializes the OpenAI embedding model with API key and options.
            *
            * @param apiKey the API key for authenticating with OpenAI's API.
            */
    public EmbeddingService() {
        OpenAiApi openAiApi = new OpenAiApi(apiKey);
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model("text-embedding-3-small")
                .build();
        this.embeddingModel = new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
    }

    /**
     * Generates a CSV-formatted string representing the embedding vector for a given input query.
     *
     * @param query the user query or text to embed.
     * @return CSV string of embedding values, or empty string if failed.
     */
    public String generateEmbeddingCSV(String query) {
        try {
            EmbeddingRequest request = new EmbeddingRequest(List.of(query), null);
            EmbeddingResponse response = embeddingModel.call(request);
            if (!response.getResults().isEmpty()) {
                float[] vector = response.getResults().get(0).getOutput();
                String csv = floatArrayToCsv(vector);
                return csv;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<Double> getEmbeddingVector(String query) {
        try {
            EmbeddingRequest request = new EmbeddingRequest(List.of(query), null);
            EmbeddingResponse response = embeddingModel.call(request);
            if (!response.getResults().isEmpty()) {
                float[] vector = response.getResults().get(0).getOutput();
                List<Double> vectorList = new ArrayList<>();
                for(float v : vector) {
                    vectorList.add(Double.valueOf(v));
                }
                return vectorList;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private String floatArrayToCsv(float[] vector) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

}