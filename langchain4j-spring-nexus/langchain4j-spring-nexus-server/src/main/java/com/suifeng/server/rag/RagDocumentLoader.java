package com.suifeng.server.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * 启动加载 Rag文档数据 (可选组件)
 */
@Configuration
public class RagDocumentLoader {

    @Autowired(required = false)
    private EmbeddingModel qwenEmbeddingModel;

    @Autowired(required = false)
    private EmbeddingStore<TextSegment> embeddingStore;

    @Bean
    @Lazy
    public ContentRetriever contentRetriever() {
        if (qwenEmbeddingModel == null || embeddingStore == null) {
            return null;
        }
        try {
            //1.加载文档
            List<Document> documents = ClassPathDocumentLoader.loadDocuments("docs");
            //2.文档切割
            DocumentByParagraphSplitter documentByParagraphSplitter =
                    new DocumentByParagraphSplitter(1000, 200);
            //3.自定义文档加载器
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(documentByParagraphSplitter)
                    .textSegmentTransformer(textSegment -> TextSegment
                            .from(textSegment.metadata().getString("file_name") +
                                    "\n" + textSegment.text(), textSegment.metadata()))
                    .embeddingModel(qwenEmbeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            ingestor.ingest(documents);
            //4.自定义内容加载器
            return EmbeddingStoreContentRetriever.builder()
                    .embeddingModel(qwenEmbeddingModel)
                    .embeddingStore(embeddingStore)
                    .maxResults(5)
                    .minScore(0.75)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}
