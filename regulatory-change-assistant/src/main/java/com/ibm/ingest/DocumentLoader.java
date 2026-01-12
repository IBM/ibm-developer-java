package com.ibm.ingest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.jboss.logging.Logger;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Startup
@Singleton
public class DocumentLoader {

    private static final Logger LOG = Logger.getLogger(DocumentLoader.class.getName());
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".pdf", ".docx", ".html");

    @Inject
    DoclingConverter doclingConverter;

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @PostConstruct
    void loadDocuments() {
        try {
            Path documentsPath = Paths.get("src/main/resources/documents");

            if (!Files.exists(documentsPath)) {
                LOG.warnf("Documents directory not found: " + documentsPath);
                return;
            }

            int successCount = 0;
            int failureCount = 0;
            int skippedCount = 0;
            int totalPages = 0;

            try (Stream<Path> paths = Files.list(documentsPath)) {
                for (Path filePath : paths.toList()) {
                    if (!Files.isRegularFile(filePath)) {
                        continue;
                    }

                    String fileName = filePath.getFileName().toString();
                    String extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();

                    if (!SUPPORTED_EXTENSIONS.contains(extension)) {
                        LOG.info("Skipping unsupported file: " + fileName);
                        skippedCount++;
                        continue;
                    }

                    try {
                        LOG.info("Processing document: " + fileName);
                        File sourceFile = filePath.toFile();

                        // Extract pages using Docling
                        List<TextSegment> segments = doclingConverter.extractPages(sourceFile);

                        // Generate embeddings and store
                        for (TextSegment segment : segments) {
                            Embedding embedding = embeddingModel.embed(segment).content();
                            embeddingStore.add(embedding, segment);
                            totalPages++;
                        }

                        successCount++;
                        LOG.info("Successfully processed: " + fileName + " (" + segments.size() + " pages)");
                    } catch (Exception e) {
                        LOG.errorf("Failed to process document: " + fileName + " - " + e.getMessage());
                        e.printStackTrace();
                        failureCount++;
                    }
                }
            }

            LOG.info(String.format(
                    "Document loading completed. Success: %d, Failures: %d, Skipped: %d, Total pages: %d",
                    successCount, failureCount, skippedCount, totalPages));
        } catch (Exception e) {
            LOG.errorf("Error loading documents: " + e.getMessage());
            e.printStackTrace();
        }
    }
}