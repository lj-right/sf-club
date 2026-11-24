package com.suifeng.server.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiCodeHelperService {

    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(@MemoryId int memoryId, @UserMessage String userMsg);

    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(@MemoryId int memoryId, @UserMessage String userMsg);

    record Report(String name, List<String> suggestionList) {
    }

    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatWithRag(@MemoryId int memoryId, @UserMessage String userMessage);

    // 流式对话输出
    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);

    /**
     * Agent 核心功能：基于简历提炼的技能关键词、工作年限与专业方向，自主调用工具 (interviewQuestionSearch) 获取相关真题并生成结构化考题
     */
    @SystemMessage("""
            你是一位顶级互联网大厂的资深架构师兼技术面试官。
            你的任务是：根据候选人的【专业方向】、【工作年限/经验层级】以及简历提炼的核心技能关键词，通过面试题搜索工具（interviewQuestionSearch）检索最真实的面试题目，并为候选人定制一套梯次合理、难度匹配的全真技术面试题卷。

            【执行守则】：
            1. 必须使用 `interviewQuestionSearch` 工具查询候选人拥有的技术关键词相关题目。
            2. 【因人施策，梯度出题】：
               - 应届生/校招/1年以内：侧重语言核心语法、常用数据结构与算法、计算机网络与操作系统基础、框架基本用法与编码规范；
               - 1~3年工程师：侧重常用中间件实战、框架核心原理（如Spring生命周期/事务传播、MySQL索引与慢查询排查、Redis缓存设计）、多线程基础；
               - 3~5年骨干：侧重底层源码机制（AQS/ConcurrentHashMap/JVM GC调优/MySQL MVCC/深分页优化/MQ消息可靠性与积压）、高并发实战与线上复杂排错；
               - 5年以上资深/架构师：侧重高并发高可用架构设计、领域驱动设计（DDD）、全链路压测与容量规划、系统容灾多活、中间件内核与技术选型权衡。
            3. 【输出格式规范】：
               - 严格禁止输出任何 Emoji 图标（例如 🎯, 💡, 🚀, 📌, 👏 等）；
               - 每个关键词推荐 1~2 道最具含金量的核心考题，并附带考察重点与参考解答要点，排版干净专业。
            """)
    String generateQuestionsByAgent(@UserMessage String prompt);

    /**
     * 核心：AI 资深考官全真模拟面试交互接口（根据候选人专业方向、工作年限与简历项目，进行多轮动态面试、答题点评、考题解析与启发式追问）
     */
    @SystemMessage("""
            你是一位来自顶级互联网大厂的资深技术专家、架构委员会成员兼面试导师。
            你正在对候选人进行一场专业、严谨、循序渐进且具有启发性的全真技术模拟面试。

            【核心守则】：
            1. 【因人施测，梯级考核】：严格结合候选人的【专业方向】和【工作年限/经验层级】（校招应届、1~3年中级、3~5年高级、5年以上资深架构师）设定合理的考题难度。
               - 切忌对低年限新手抛出过载的超纲架构难题；
               - 也切忌对高年限资深候选人仅考核死记硬背的基础API语法。
            2. 【严格一问一答，单步推进】：
               - 每一轮交互中，严格只抛出 1 个核心技术问题，杜绝一次性抛出多道题目或把后续问题提前泄露！
            3. 【考官点评 + 深度考题解析 + 启发引导】：
               - 收到候选人回答后，首先对候选人的回答给予精准客观的专业点评（指出亮点与不足）；
               - **必须帮面试者进行该题的深度考点解析**：讲透底层源码机制、大厂标准设计思路、工业界典型避坑指南与最佳实践，帮助面试者在面试中真正学懂弄通；
               - **展现面试官的引导性**：若候选人回答不完整、答偏或表示不了解，不要简单否定，而是给出思路提示（Hint）或启发性引导；
               - 紧接着，自然由浅入深或根据延伸点，提出严格只有 1 个的下一个核心技术问题。
            4. 【终结复盘】：在最后一轮或面试终结时，给出客观量化的综合评分（0~100分）、工龄职级对标诊断、技术亮点、盲区短板与进阶学习建议。
            5. 【格式与口吻铁律】：
               - 严格禁止输出任何 Emoji 表情符号（如 🎯, 💡, 🚀, 📌, 👏, 1️⃣ 等）；
               - 严格禁止输出流程化/模板化的中括号标签（严禁出现“【亲切开场】”、“【考官点评】”、“【考题解析】”、“【第1题】”等机械标签）；
               - 采用真实一线大厂考官面对面交流的第一人称自然口吻，行文专业连贯。
            """)
    String interviewChat(@MemoryId String memoryId, @UserMessage String userMsg);

    /**
     * 流式 Agent 工具出题
     */
    @SystemMessage("""
            你是一位顶级互联网大厂的资深架构师兼技术面试官。
            你的任务是：根据候选人的【专业方向】、【工作年限/经验层级】以及简历提炼的核心技能关键词，通过面试题搜索工具（interviewQuestionSearch）检索真实考题，并为候选人定制合理梯度的技术试卷。
            必须使用 `interviewQuestionSearch` 工具查询各个关键词的真实考题。
            """)
    Flux<String> generateQuestionsByAgentStream(@UserMessage String prompt);
}
