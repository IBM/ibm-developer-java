package com.ibm.ai;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;

/**
 * Custom ContentInjector that provides enhanced metadata formatting
 * with better structure for document citations and retrieval information.
 */
public class CustomContentInjector extends DefaultContentInjector {

    private final List<String> metadataKeysToInclude;

    public CustomContentInjector(PromptTemplate promptTemplate, List<String> metadataKeysToInclude) {
        super(promptTemplate, metadataKeysToInclude);
        this.metadataKeysToInclude = metadataKeysToInclude;
    }

    @Override
    protected String format(List<Content> contents) {
        if (contents.isEmpty()) {
            return "";
        }
        
        // Format each content block with clear separation and numbering
        String separator = "\n\n" + "=".repeat(80) + "\n\n";
        return IntStream.range(0, contents.size())
                .mapToObj(i -> formatContentBlock(contents.get(i), i + 1))
                .collect(Collectors.joining(separator));
    }

    /**
     * Formats a single content block with metadata at the top for clarity.
     */
    private String formatContentBlock(Content content, int blockNumber) {
        TextSegment segment = content.textSegment();
        Metadata metadata = segment.metadata();

        if (metadataKeysToInclude == null || metadataKeysToInclude.isEmpty()) {
            return String.format("--- Content Block %d ---\n%s", blockNumber, segment.text());
        }

        StringBuilder formatted = new StringBuilder();
        
        // Add block number and separator
        formatted.append("--- Content Block ").append(blockNumber).append(" ---\n");
        
        // Format document metadata FIRST (for citations) - most important
        String docMetadata = formatDocumentMetadata(metadata);
        if (!docMetadata.isEmpty()) {
            formatted.append("Document Information:\n");
            formatted.append(docMetadata);
            formatted.append("\n");
        }
        
        // Add the content text
        formatted.append("\nContent:\n");
        formatted.append(segment.text());
        
        // Format retrieval metadata (for transparency) - less prominent
        String retrievalMetadata = formatRetrievalMetadata(metadata);
        if (!retrievalMetadata.isEmpty()) {
            formatted.append("\n\nRetrieval Information:\n");
            formatted.append(retrievalMetadata);
        }

        return formatted.toString();
    }

    /**
     * Formats document-related metadata (doc_id, page_number, document_type, file_name)
     * in a structured way for citations.
     */
    private String formatDocumentMetadata(Metadata metadata) {
        StringBuilder docInfo = new StringBuilder();
        
        // Document identification
        if (metadataKeysToInclude.contains("doc_id") && metadata.getString("doc_id") != null) {
            docInfo.append("  Document ID: ").append(metadata.getString("doc_id"));
        }
        
        if (metadataKeysToInclude.contains("file_name") && metadata.getString("file_name") != null) {
            if (docInfo.length() > 0) docInfo.append("\n");
            docInfo.append("  File Name: ").append(metadata.getString("file_name"));
        }
        
        if (metadataKeysToInclude.contains("page_number") && metadata.getString("page_number") != null) {
            if (docInfo.length() > 0) docInfo.append("\n");
            docInfo.append("  Page: ").append(metadata.getString("page_number"));
        }
        
        if (metadataKeysToInclude.contains("document_type") && metadata.getString("document_type") != null) {
            if (docInfo.length() > 0) docInfo.append("\n");
            docInfo.append("  Type: ").append(metadata.getString("document_type"));
        }
        
        return docInfo.toString();
    }

    /**
     * Formats retrieval-related metadata (retrieval_method, similarity_score, retrieval_timestamp)
     * for transparency about how the content was found.
     */
    private String formatRetrievalMetadata(Metadata metadata) {
        StringBuilder retrievalInfo = new StringBuilder();
        
        if (metadataKeysToInclude.contains("retrieval_method") && metadata.getString("retrieval_method") != null) {
            retrievalInfo.append("  Method: ").append(metadata.getString("retrieval_method"));
        }
        
        if (metadataKeysToInclude.contains("similarity_score") && metadata.getString("similarity_score") != null) {
            if (retrievalInfo.length() > 0) retrievalInfo.append("\n");
            retrievalInfo.append("  Similarity Score: ").append(metadata.getString("similarity_score"));
        }
        
        if (metadataKeysToInclude.contains("retrieval_timestamp") && metadata.getString("retrieval_timestamp") != null) {
            if (retrievalInfo.length() > 0) retrievalInfo.append("\n");
            retrievalInfo.append("  Retrieved At: ").append(metadata.getString("retrieval_timestamp"));
        }
        
        return retrievalInfo.toString();
    }
}
