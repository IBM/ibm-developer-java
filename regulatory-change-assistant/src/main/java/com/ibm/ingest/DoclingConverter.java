package com.ibm.ingest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ai.docling.serve.api.chunk.response.Chunk;
import ai.docling.serve.api.chunk.response.ChunkDocumentResponse;
import ai.docling.serve.api.convert.request.options.OutputFormat;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import io.quarkiverse.docling.runtime.client.DoclingService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DoclingConverter {

    @Inject
    DoclingService doclingService;

    public List<TextSegment> extractPages(File sourceFile) throws IOException {
        Path filePath = sourceFile.toPath();
        String fileName = sourceFile.getName();

        // Use Docling to chunk the document
        ChunkDocumentResponse chunkResponse = doclingService.chunkFileHybrid(filePath, OutputFormat.MARKDOWN);

        List<Chunk> chunks = chunkResponse.getChunks();

        // Group chunks by page number
        Map<Integer, StringBuilder> pageTextMap = new HashMap<>();
        for (Chunk chunk : chunks) {
            List<Integer> pageNumbers = chunk.getPageNumbers();
            if (pageNumbers != null && !pageNumbers.isEmpty()) {
                for (Integer pageNumber : pageNumbers) {
                    pageTextMap.computeIfAbsent(pageNumber, k -> new StringBuilder())
                            .append(chunk.getText())
                            .append("\n\n");
                }
            } else {
                // If no page numbers, assign to page 1
                pageTextMap.computeIfAbsent(1, k -> new StringBuilder())
                        .append(chunk.getText())
                        .append("\n\n");
            }
        }

        // Create TextSegments with metadata
        return pageTextMap.entrySet().stream()
                .map(entry -> {
                    int pageNumber = entry.getKey();
                    String text = entry.getValue().toString().trim();

                    // Extract file extension to determine document type
                    String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                    String documentType = switch (extension) {
                        case "pdf" -> "PDF";
                        case "docx" -> "DOCX";
                        case "html", "htm" -> "HTML";
                        default -> "UNKNOWN";
                    };

                    Metadata metadata = Metadata.from(Map.of(
                            "doc_id", fileName,
                            "page_number", String.valueOf(pageNumber),
                            "document_type", documentType,
                            "file_name", fileName));

                    return TextSegment.from(text, metadata);
                })
                .collect(Collectors.toList());
    }
}