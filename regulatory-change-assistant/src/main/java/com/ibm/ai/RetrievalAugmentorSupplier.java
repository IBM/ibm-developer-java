package com.ibm.ai;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.function.Supplier;

import com.ibm.retrieval.DocumentRetriever;

import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.injector.ContentInjector;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RetrievalAugmentorSupplier implements Supplier<RetrievalAugmentor> {

    @Inject
    DocumentRetriever documentRetriever;

    @Override
    public RetrievalAugmentor get() {

        PromptTemplate promptTemplate = PromptTemplate.from(
                """
                        {{userMessage}}

                        Answer using the following information:
                        {{contents}}

                        When citing sources, use the Document Information provided with each content block.
                        Format citations as: [Document: doc_id, Page: page_number]""");

        List<String> metadataKeys = asList(
                "doc_id",
                "page_number",
                "document_type",
                "file_name",
                "retrieval_method",
                "similarity_score",
                "retrieval_timestamp");

        ContentInjector contentInjector = new CustomContentInjector(promptTemplate, metadataKeys);

        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(documentRetriever)
                .contentInjector(contentInjector)
                .build();
    }
}