package com.chatbot.searcher;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.*;

public class LuceneSearcher {

    private final IndexSearcher searcher;
    private final QueryParser parser;

    public LuceneSearcher(String indexPath) throws Exception {
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(dir);
        this.searcher = new IndexSearcher(reader);
        this.parser = new QueryParser("content", new StandardAnalyzer());
    }

    public List<String> search(String queryStr) throws Exception {
        List<String> results = new ArrayList<>();
        Query query = parser.parse(queryStr);
        TopDocs topDocs = searcher.search(query, 10); // Fetch top 10 hits

        if (topDocs.scoreDocs.length == 0) return results;

        // 🔍 Manually find max score
        float maxScore = Arrays.stream(topDocs.scoreDocs)
                .map(scoreDoc -> scoreDoc.score)
                .max(Float::compare)
                .orElse(0f);

        float threshold = maxScore * 0.2f; // only allow chunks with >= 20% max score

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            if (scoreDoc.score >= threshold) {
                Document doc = searcher.doc(scoreDoc.doc);
                String content = doc.get("content");
                if (content != null) {
                    results.add(content);
                }
            }
        }

        return results;
    }
}
