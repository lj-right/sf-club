package com.suifeng.server.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.IngestionResult;
import jakarta.annotation.Resource;
import net.bytebuddy.dynamic.ClassFileLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * 启动加载 Rag文档数据
 */
@Configuration
public class RagDocumentLoader {
    @Resource
    private EmbeddingModel qwenEmbeddingModel;

    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;

    @Bean
    @Lazy
    public ContentRetriever contentRetriever() {
        //1.加载文档FileSystemDocumentLoader
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("docs");
        //2.文档切割：按段落切割，最大1000字符，重叠200各字符
        DocumentByParagraphSplitter documentByParagraphSplitter =
                new DocumentByParagraphSplitter(1000, 200);
        //3.自定义文档加载器，把文档转换为向量并存储到向量数据库中
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentByParagraphSplitter)
                //为每个切片后的文档（TextSegment）添加文档名称作为元信息
                .textSegmentTransformer(textSegment -> TextSegment
                        .from(textSegment.metadata().getString("file_name") +
                                "\n" + textSegment.text(), textSegment.metadata()))
                //使用向量模型
                .embeddingModel(qwenEmbeddingModel)
                //向量存储store
                .embeddingStore(embeddingStore)
                .build();

        //加载文档
        ingestor.ingest(documents);
        //4.自定义内容加载器
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(5)
                .minScore(0.75) //过滤掉分数小于0.75的结果
                .build();
        return contentRetriever;
    }

}
