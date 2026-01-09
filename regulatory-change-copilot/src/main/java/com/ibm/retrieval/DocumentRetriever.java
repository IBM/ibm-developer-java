package com.ibm.retrieval;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DocumentRetriever implements dev.langchain4j.rag.content.retriever.ContentRetriever {

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    private static final int MAX_RESULTS = 5;
    private static final double MIN_SCORE = 0.7;

    @Override
    public List<Content> retrieve(Query query) {
        String queryText = query.text();
        // Convert query text to embedding vector
        Embedding queryEmbedding = embeddingModel.embed(queryText).content();

        // Search embedding store for similar documents
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(MAX_RESULTS)
                .minScore(MIN_SCORE)
                .build();

        EmbeddingSearchResult<TextSegment> results = embeddingStore.search(searchRequest);

        // Enrich with metadata and return
        return results.matches().stream()
                .map(match -> {
                    TextSegment segment = match.embedded();

                    // Add retrieval metadata
                    dev.langchain4j.data.document.Metadata enriched = dev.langchain4j.data.document.Metadata
                            .from(segment.metadata().toMap());
                    enriched.put("retrieval_method", "vector_search");
                    enriched.put("similarity_score", String.valueOf(match.score()));
                    enriched.put("retrieval_timestamp", Instant.now().toString());

                    TextSegment enrichedSegment = TextSegment.from(segment.text(), enriched);
                    return Content.from(enrichedSegment);
                })
                .collect(Collectors.toList());
    }
}