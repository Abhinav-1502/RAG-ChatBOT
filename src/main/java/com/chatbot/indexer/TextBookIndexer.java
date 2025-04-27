package com.chatbot.indexer;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TextBookIndexer {

    private static final int MIN_CHUNK_SIZE = 300; // Characters
    private static final int MAX_CHUNK_SIZE = 800;

    private static final String CHUNK_OUTPUT_FILE = "src/main/resources/kratos_chunks.txt";

    public static void indexTextbook(String filePath) throws IOException {

        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        List<String> chunks = smartChunk(content);

        // Prepare to write chunks to txt file
        BufferedWriter chunkWriter = new BufferedWriter(new FileWriter(CHUNK_OUTPUT_FILE));

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i).replaceAll("\\s+", " ").trim();

            // Save chunk to file
            chunkWriter.write(chunk.length()+": "+chunk);
            chunkWriter.newLine();
            chunkWriter.newLine();
        }

        chunkWriter.close();
        System.out.println("✅ Indexing complete with smart chunking!");
        System.out.println("✅ Chunks also saved to " + CHUNK_OUTPUT_FILE);
    }

    private static List<String> smartChunk(String content) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = content.split("\\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) continue;

            if (currentChunk.length() + paragraph.length() < MAX_CHUNK_SIZE) {
                currentChunk.append(paragraph).append("\n\n");
            } else {
                if (currentChunk.length() >= MIN_CHUNK_SIZE) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
                currentChunk.append(paragraph).append("\n\n");
            }
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }
//
    public static void main(String[] args) throws IOException {
        String filePath = "src/main/resources/RawData/kratos.txt";
        indexTextbook(filePath);
    }
}