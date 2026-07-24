package com.suifeng.server.controller;

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByWordSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/rag")
@Slf4j
public class RagController {

    private final   EmbeddingModel qwenEmbeddingModel;

    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * RAG数据入库
     *
     * @return 数量
     */
    @GetMapping("/save")
    public Object chat() {
        // 1️⃣ 使用 Tika 解析 docx/pdf 等文件
        var documents = FileSystemDocumentLoader.loadDocuments(
                "C:\\Users\\25217\\myExample",
                new ApacheTikaDocumentParser()
        );
        // 2️⃣ 定义自定义的文本拆分器 chunkSize=100 表示每段最大 500 tokens，overlap=30 表示重叠 30 tokens
        var splitter = new DocumentByWordSplitter(100, 30);
        // 3️⃣ 构建带自定义拆分器的 ingestor
        var ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        // 4️⃣ 执行嵌入生成与存储
        ingestor.ingest(documents);
        return documents.size();
    }

    /**
     * RAG数据查询
     *
     * @param query 查询
     * @return 结果
     */
    @GetMapping("/search")
    public Object search(@RequestParam String query) {
        var searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(qwenEmbeddingModel.embed(TextSegment.from(query)).content())
                .maxResults(5)
                .minScore(0.6)
                .build();

        var matches = embeddingStore.search(searchRequest).matches();
        var results = matches.stream()
                .map(match -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("embeddingScore", match.score());
                    map.put("embeddingId", match.embeddingId());
                    return map;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

}
