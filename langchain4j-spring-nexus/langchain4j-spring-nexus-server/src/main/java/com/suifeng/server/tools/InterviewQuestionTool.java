package com.suifeng.server.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * AI Agent 面试题核心工具：根据技能关键词检索相关面试题目
 */
@Component
@Slf4j
public class InterviewQuestionTool {

    private static final Map<String, List<String>> FALLBACK_QUESTION_BANK = new HashMap<>();

    static {
        FALLBACK_QUESTION_BANK.put("redis", List.of(
                "Redis为什么这么快？底层IO多路复用模型原理是什么？",
                "Redis持久化机制RDB和AOF的区别与生产环境推荐策略？",
                "Redis缓存穿透、缓存击穿、缓存雪崩的成因及企业级解决方案？",
                "Redis分布式锁的实现原理？Redisson的看门狗机制与Redlock算法？",
                "Redis常见数据结构（String、Hash、List、Set、ZSet）底层实现原理（跳表、SDS、ziplist）？"
        ));
        FALLBACK_QUESTION_BANK.put("mysql", List.of(
                "MySQL InnoDB引擎的索引底层为什么用B+树而不是B树或红黑树？",
                "MySQL事务四大特性（ACID）及实现机制（Redo Log、Undo Log、MVCC）？",
                "MySQL聚簇索引和非聚簇索引、回表查询与覆盖索引的区别？",
                "深分页问题（limit 1000000, 10）为什么慢？有哪些优化手段？",
                "MySQL的慢查询如何排查与优化？Explain执行计划重点关注哪些指标？"
        ));
        FALLBACK_QUESTION_BANK.put("spring", List.of(
                "Spring Bean的生命周期全流程是怎样的？",
                "Spring如何解决三级缓存循环依赖问题？为什么必须是三级缓存？",
                "Spring事务失效的常见场景有哪些？底层原理是什么？",
                "Spring AOP底层动态代理（JDK动态代理 vs CGLIB）的区别与应用？",
                "Spring Boot自动装配原理（@EnableAutoConfiguration与META-INF/spring.factories）？"
        ));
        FALLBACK_QUESTION_BANK.put("java", List.of(
                "Java中HashMap底层数据结构、扩容机制及为什么JDK1.8引入红黑树？",
                "JVM内存区域划分与垃圾收集器（CMS、G1、ZGC）的演进与核心区别？",
                "Java线程池核心参数有哪些？拒绝策略有哪些？生产环境如何合理设置核心线程数？",
                "AQS（AbstractQueuedSynchronizer）的底层实现机制与ReentrantLock的公平/非公平锁实现？",
                "Java并发关键字volatile的可见性与有序性原理（内存屏障与MESI缓存一致性协议）？"
        ));
        FALLBACK_QUESTION_BANK.put("kafka", List.of(
                "Kafka如何保证消息不丢失、不重复消费（Exactly-Once语义）？",
                "Kafka高性能高吞吐的核心原因（顺序读写、PageCache、零拷贝、批量发送）？",
                "Kafka的Rebalance机制是什么？什么情况下会触发Rebalance？"
        ));
        FALLBACK_QUESTION_BANK.put("微服务", List.of(
                "微服务架构下服务注册与发现（Nacos/Eureka）心跳与健康检查机制？",
                "分布式事务常见解决方案（2PC、TCC、Saga、Seata AT模式）？",
                "网关（Gateway）的作用与核心过滤器机制？如何实现限流与熔断？"
        ));
    }

    /**
     * Agent 工具：通过关键词获取相关技术面试题
     *
     * @param keyword 搜索关键词（如 "Redis"、"Java多线程"、"Spring Boot" 等）
     * @return 面试题列表文本
     */
    @Tool(name = "interviewQuestionSearch", value = """
            Retrieves relevant interview questions for technical topics based on a keyword.
            Use this tool when you need interview questions for specific skills extracted from a resume,
            such as 'Redis', 'MySQL', 'Spring', 'Java Concurrent', 'Kafka', etc.
            The input must be a clear technology keyword.
            """
    )
    public String searchInterviewQuestions(@P(value = "the keyword to search, e.g. 'Redis', 'MySQL', 'Spring Boot'") String keyword) {
        log.info(">>> [Agent Tool: interviewQuestionSearch] 正在为关键词【{}】检索相关高频面试题...", keyword);
        if (!StringUtils.hasText(keyword)) {
            return "关键词为空，未能检索到题目。";
        }

        List<String> questions = new ArrayList<>();
        try {
            String encodedKeyword = URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8);
            String url = "https://www.mianshiya.com/search/all?searchText=" + encodedKeyword;
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(4000)
                    .get();

            Elements questionElements = doc.select(".ant-table-cell > a");
            for (org.jsoup.nodes.Element el : questionElements) {
                String text = el.text().trim();
                if (StringUtils.hasText(text) && !questions.contains(text)) {
                    questions.add(text);
                }
            }
        } catch (IOException e) {
            log.warn("[Agent Tool] 网络检索异常或超时，启用高质量精选真题库降级兜底: {}", e.getMessage());
        }

        // 如果网络检索不到，使用知识图谱真题库兜底
        if (questions.isEmpty()) {
            questions = findFallbackQuestions(keyword);
        }

        log.info("<<< [Agent Tool: interviewQuestionSearch] 关键词【{}】成功检索到 {} 道面试题", keyword, questions.size());
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【针对技能关键词 '%s' 检索到的真实面试考题清单】\n", keyword));
        for (int i = 0; i < questions.size(); i++) {
            sb.append(String.format("%d. %s\n", i + 1, questions.get(i)));
        }
        return sb.toString();
    }

    /**
     * 本地精选真题库快速匹配
     */
    private List<String> findFallbackQuestions(String keyword) {
        String lower = keyword.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, List<String>> entry : FALLBACK_QUESTION_BANK.entrySet()) {
            if (lower.contains(entry.getKey()) || entry.getKey().contains(lower)) {
                return entry.getValue();
            }
        }
        // 通用兜底
        return List.of(
                String.format("请结合项目实践，谈谈你对 %s 的核心架构设计与底层实现原理？", keyword),
                String.format("在生产高并发或大数据场景下，使用 %s 遇到过哪些性能瓶颈？如何调优解决？", keyword),
                String.format("%s 的典型故障排查思路与最佳实践方案？", keyword)
        );
    }
}
