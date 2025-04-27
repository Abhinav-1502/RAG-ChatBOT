package com.chatbot;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;

public class ChunkStats {
    public static void main(String[] args) throws IOException {
        String CHUNK_FILE_PATH = "src/main/resources/chunks.txt";
        List<String> lines = Files.readAllLines(Paths.get(CHUNK_FILE_PATH));

        long validChunkCount = lines.stream()
                .map(line -> line.replaceAll("\\s+", " ").trim())
                .filter(cleaned -> cleaned.length() >= 30)
                .count();

        System.out.println("📦 Total valid chunks (length >= 30): " + validChunkCount);
    }
}