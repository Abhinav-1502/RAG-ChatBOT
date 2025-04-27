package com.chatbot.nlp;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.util.BytesRef;

import java.util.*;
import java.util.stream.Collectors;

public class QueryPreprocessor {

    private static final Set<String> STOPWORDS;

    static {
        Set<String> stopwords = new HashSet<>();
        EnglishAnalyzer.getDefaultStopSet().forEach(token -> stopwords.add(token.toString()));
        STOPWORDS = stopwords;
    }

    public static String preprocess(String query) {
        if (query == null || query.isBlank()) return "";

        // Normalize: lowercase, strip punctuation, extra spaces
        String normalized = query.toLowerCase().replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ");

        // Tokenize
        List<String> tokens = Arrays.asList(normalized.split(" "));

        // Stopword removal, short word filter, and simple lemmatization
        List<String> cleanedTokens = tokens.stream()
                .map(QueryPreprocessor::stem)
                .filter(token -> token.length() >= 3)
                .filter(token -> !STOPWORDS.contains(token))
                .collect(Collectors.toList());

        return String.join(" ", cleanedTokens);
    }

    // Naive stemmer (optional improvement)
    private static String stem(String word) {
        if (word.endsWith("ing") && word.length() > 5) return word.substring(0, word.length() - 3);
        if (word.endsWith("ed") && word.length() > 4) return word.substring(0, word.length() - 2);
        if (word.endsWith("s") && word.length() > 3) return word.substring(0, word.length() - 1);
        return word;
    }
}