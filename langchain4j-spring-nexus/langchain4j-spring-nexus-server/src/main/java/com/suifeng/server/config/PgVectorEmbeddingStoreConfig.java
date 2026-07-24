package com.suifeng.server.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 使用 PostgreSQL + pgvector 作为向量存储。
 */
@Configuration
public class PgVectorEmbeddingStoreConfig {

    @Bean(name = {"embeddingStore", "qwenEmbeddingStore"})
    public EmbeddingStore<TextSegment> embeddingStore(DataSource dataSource,
                                                      EmbeddingModel qwenEmbeddingModel) {
        return PgVectorEmbeddingStore.datasourceBuilder()
                .datasource(dataSource)
                .table("embeddings")
                .dimension(qwenEmbeddingModel.dimension())
                .createTable(true)
                .dropTableFirst(false)
                .build();
    }
}
