package com.chatbot.dto;

import java.util.List;

public class LLMRequest {
    private String question;
    private List<String> contextChunks;

    public LLMRequest() {}

    public LLMRequest(String question, List<String> contextChunks) {
        this.question = question;
        this.contextChunks = contextChunks;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getContextChunks() {
        return contextChunks;
    }

    public void setContextChunks(List<String> contextChunks) {
        this.contextChunks = contextChunks;
    }
}