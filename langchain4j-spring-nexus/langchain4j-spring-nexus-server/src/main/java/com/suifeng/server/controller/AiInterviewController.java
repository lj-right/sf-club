package com.suifeng.server.controller;

import com.suifeng.server.ai.AiCodeHelperService;
import com.suifeng.server.ai.AiCodeHelperServiceFactory;
import com.suifeng.server.controller.dto.AgentInterviewReq;
import com.suifeng.server.controller.dto.CommonResult;
import com.suifeng.server.controller.dto.InterviewSessionReq;
import com.suifeng.server.model.AiModelConfigDto;
import com.suifeng.server.model.DynamicChatModelFactory;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * AI 简历分析与 Agent 工具出题控制器
 */
@RestController
@RequestMapping("/ai/interview")
@Slf4j
public class AiInterviewController {

    @Resource
    private AiCodeHelperService defaultAiService;

    @Resource
    private AiCodeHelperServiceFactory aiServiceFactory;

    @Resource
    private DynamicChatModelFactory dynamicChatModelFactory;

    private static final Tika TIKA = new Tika();

    /**
     * 会话级简历知识库文本缓存 (memoryId -> resumeText)
     */
    private static final Map<String, String> RESUME_KNOWLEDGE_MAP = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 会话级候选人元信息缓存 (memoryId -> ProfileMap)
     */
    private static final Map<String, Map<String, Object>> SESSION_PROFILE_MAP = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 1. Agent 工具联动出题接口：基于简历提炼出的关键词、工作年限与专业方向，Agent 自动调用工具检索出契合难度的权威题目
     */
    @PostMapping("/generate-questions")
    public CommonResult<Map<String, Object>> generateQuestions(@RequestBody AgentInterviewReq req) {
        try {
            if (req == null || CollectionUtils.isEmpty(req.getKeywords())) {
                return CommonResult.fail("技术关键词列表不能为空！");
            }

            String jobRole = resolveJobRole(req.getJobRole(), null, null, req.getKeywords());
            String yearsExp = resolveYearsExp(req.getYearsExp(), null, null);
            String level = getSeniorityLevel(yearsExp);

            log.info("接收到 Agent 题库生成请求，岗位方向: {}, 工作年限: {}({}), 关键词: {}, 模型: {}",
                    jobRole, yearsExp, level, req.getKeywords(),
                    req.getModelConfig() != null ? req.getModelConfig().getMode() : "系统默认");

            AiCodeHelperService service = req.getModelConfig() != null
                    ? aiServiceFactory.createService(req.getModelConfig())
                    : defaultAiService;

            String prompt = buildAgentPrompt(req, jobRole, yearsExp, level);
            String aiResult = sanitizeInterviewReply(service.generateQuestionsByAgent(prompt));

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("keywords", req.getKeywords());
            resultMap.put("jobRole", jobRole);
            resultMap.put("yearsExp", yearsExp);
            resultMap.put("experienceLevel", level);
            resultMap.put("content", aiResult);
            resultMap.put("agentExecuted", true);

            return CommonResult.ok(resultMap);
        } catch (Exception e) {
            log.error("Agent 智能检索出题失败", e);
            return CommonResult.fail("Agent 出题异常: " + e.getMessage());
        }
    }

    /**
     * 1.1 核心：根据候选人专业方向、工作年限及简历提炼的关键字正式开启 AI 模拟面试
     * 考官结合岗位方向、工龄梯度和简历项目做亲切开场，并使用工具检索真实考题，抛出匹配难度的第 1 题
     */
    @PostMapping("/start-interview")
    public CommonResult<Map<String, Object>> startInterview(@RequestBody InterviewSessionReq req) {
        try {
            if (req == null || CollectionUtils.isEmpty(req.getKeywords())) {
                return CommonResult.fail("简历技术关键词不能为空！");
            }
            String memoryId = StringUtils.hasText(req.getMemoryId()) ? req.getMemoryId() : "interview_" + System.currentTimeMillis();
            req.setMemoryId(memoryId);

            String resumeText = req.getResumeText();
            if (StringUtils.hasText(resumeText)) {
                RESUME_KNOWLEDGE_MAP.put(memoryId, resumeText);
            } else {
                resumeText = RESUME_KNOWLEDGE_MAP.get(memoryId);
            }

            // 解析候选人画像与工龄等级
            String candidateName = resolveCandidateName(req.getProfile());
            String jobRole = resolveJobRole(req.getJobRole(), req.getProfile(), resumeText, req.getKeywords());
            String yearsExp = resolveYearsExp(req.getYearsExp(), req.getProfile(), resumeText);
            String level = getSeniorityLevel(yearsExp);

            // 缓存元数据供多轮对话取用
            Map<String, Object> sessionProfile = new HashMap<>();
            sessionProfile.put("candidateName", candidateName);
            sessionProfile.put("jobRole", jobRole);
            sessionProfile.put("yearsExp", yearsExp);
            sessionProfile.put("level", level);
            if (!CollectionUtils.isEmpty(req.getKeywords())) {
                sessionProfile.put("keywords", req.getKeywords());
            }
            SESSION_PROFILE_MAP.put(memoryId, sessionProfile);

            log.info("开启 AI 模拟面试: 会话ID={}, 候选人={}, 岗位方向={}, 工作年限={}({}), 关键词={}, 简历字数={}, 模型={}",
                    memoryId, candidateName, jobRole, yearsExp, level, req.getKeywords(),
                    resumeText != null ? resumeText.length() : 0,
                    req.getModelConfig() != null ? req.getModelConfig().getMode() : "系统默认");

            AiCodeHelperService service = req.getModelConfig() != null
                    ? aiServiceFactory.createService(req.getModelConfig())
                    : defaultAiService;

            StringBuilder prompt = new StringBuilder();
            if (StringUtils.hasText(resumeText)) {
                prompt.append("【候选人会话专属档案知识库】：\n");
                prompt.append("```\n");
                prompt.append(resumeText.length() > 3000 ? resumeText.substring(0, 3000) : resumeText).append("\n");
                prompt.append("```\n\n");
            }

            prompt.append("【候选人画像特征】：\n");
            prompt.append("- 候选人姓名：").append(candidateName).append("\n");
            prompt.append("- 应聘专业方向：").append(jobRole).append("\n");
            prompt.append("- 工作年限/工龄：").append(yearsExp).append(" (判定考核层级：").append(level).append(")\n");
            if (!CollectionUtils.isEmpty(req.getKeywords())) {
                prompt.append("- 本场核心技术栈维度：").append(String.join("、", req.getKeywords())).append("\n");
            }
            prompt.append("\n");

            // 注入针对该岗位和工龄梯度的考官指导规范
            prompt.append(buildInterviewEvaluationStandard(jobRole, yearsExp, level));
            prompt.append("\n");

            prompt.append("【本轮面试官提问任务（严格一问一答）】：\n");
            prompt.append("你正在与候选人面对面交流，请以一线大厂资深技术专家兼导师的专业、亲切口吻执行：\n");
            prompt.append("1. 自然开场：先用 1~2 句专业、亲切的话向候选人打招呼，明确提及你认真研读了其简历，注意到了其在【").append(jobRole)
                    .append("】方向及具体负责的项目技术选型；\n");
            prompt.append("2. 提出第 1 题：紧扣候选人简历中真实落地的核心项目场景或关键技术栈，抛出与【").append(yearsExp)
                    .append("】经验深度严格匹配的第 1 个核心技术问题（可参考检索工具 `interviewQuestionSearch` 获取权威真题）；\n");
            prompt.append("3. 严格一问一答：严格只提出 1 个核心技术问题！切记绝不要一次性抛出多道题目，绝不要提前透露后续考题或答案，邀请候选人作答；\n");
            prompt.append("4. 【格式与口吻铁律】：\n");
            prompt.append("   - 严禁输出任何 Emoji 表情图标（如 🎯、💡、🚀、📌、👏 等）；\n");
            prompt.append("   - 严禁输出任何流程化机械标签（如“【亲切开场】”、“【第1题】”、“【考察重点】”等字样严禁出现）；\n");
            prompt.append("   - 像真人面试官面对面交谈一样自然表达，直接输出对话内容。");

            String reply = sanitizeInterviewReply(service.interviewChat(memoryId, prompt.toString()));

            Map<String, Object> data = new HashMap<>();
            data.put("memoryId", memoryId);
            data.put("currentStep", 1);
            data.put("totalSteps", req.getTotalSteps() != null ? req.getTotalSteps() : 4);
            data.put("jobRole", jobRole);
            data.put("yearsExp", yearsExp);
            data.put("experienceLevel", level);
            data.put("reply", reply);
            data.put("isFinished", false);
            return CommonResult.ok(data);
        } catch (Exception e) {
            log.error("开启 AI 模拟面试失败", e);
            return CommonResult.fail("开启面试异常: " + e.getMessage());
        }
    }

    /**
     * 1.2 核心：候选人作答与 AI 面试官多轮动态交互（智能点评、梯级追问、下一技能考察、终结评分）
     */
    @PostMapping("/chat")
    public CommonResult<Map<String, Object>> interviewChat(@RequestBody InterviewSessionReq req) {
        try {
            if (req == null || !StringUtils.hasText(req.getMemoryId())) {
                return CommonResult.fail("面试会话 memoryId 不能为空！");
            }
            if (!StringUtils.hasText(req.getUserAnswer())) {
                return CommonResult.fail("作答内容不能为空！");
            }

            int step = req.getCurrentStep() != null ? req.getCurrentStep() : 1;
            int total = req.getTotalSteps() != null ? req.getTotalSteps() : 4;
            boolean isFinish = Boolean.TRUE.equals(req.getFinish()) || step >= total;

            String resumeText = StringUtils.hasText(req.getResumeText()) ? req.getResumeText() : RESUME_KNOWLEDGE_MAP.get(req.getMemoryId());
            Map<String, Object> sessionProfile = SESSION_PROFILE_MAP.getOrDefault(req.getMemoryId(), Collections.emptyMap());

            String jobRole = StringUtils.hasText(req.getJobRole()) ? req.getJobRole() : String.valueOf(sessionProfile.getOrDefault("jobRole", "Java 后端开发"));
            String yearsExp = StringUtils.hasText(req.getYearsExp()) ? req.getYearsExp() : String.valueOf(sessionProfile.getOrDefault("yearsExp", "1-3年"));
            String level = getSeniorityLevel(yearsExp);
            List<String> keywords = !CollectionUtils.isEmpty(req.getKeywords()) ? req.getKeywords() : (List<String>) sessionProfile.get("keywords");

            log.info("AI 面试多轮对话: 会话ID={}, 当前第{}轮(共{}轮), 岗位={}, 工龄={}({}), 是否终结={}",
                    req.getMemoryId(), step, total, jobRole, yearsExp, level, isFinish);

            AiCodeHelperService service = req.getModelConfig() != null
                    ? aiServiceFactory.createService(req.getModelConfig())
                    : defaultAiService;

            StringBuilder prompt = new StringBuilder();
            prompt.append("【候选人画像背景】：\n");
            prompt.append("- 专业方向：").append(jobRole).append("\n");
            prompt.append("- 工作年限/工龄：").append(yearsExp).append(" (考核梯度：").append(level).append(")\n\n");

            prompt.append("候选人刚才的作答内容如下：\n");
            prompt.append("```\n").append(req.getUserAnswer()).append("\n```\n\n");

            if (isFinish) {
                prompt.append("【指令：本场面试进入最终总结复盘阶段】\n");
                prompt.append("本场全真技术模拟面试已完成所有预定轮次考察。\n");
                prompt.append("请你以资深大厂技术面试考官身份，对标候选人申报的【专业方向：").append(jobRole)
                        .append("】与【工作年限：").append(yearsExp).append(" (").append(level).append(")】标准，完成全面专业的面试复盘总结：\n");
                prompt.append("1. 对候选人的最后一题回答进行简明扼要的专业评析；\n");
                prompt.append("2. 对整场面试进行全局深度复盘与对标诊断：\n");
                prompt.append("   - 综合技术评分：给出明确客观的分数（0~100分）；\n");
                prompt.append("   - 工龄与职级对标诊断：对照行业内该工龄（").append(yearsExp).append("）的平均水平与大厂职级要求，明确给出定位判定（如：低于预期/基本达标/完全符合预期/超出预期具备更高阶潜力）；\n");
                prompt.append("   - 工程与技术实战亮点：指出展现出的良好工程素养、底层理解或架构意识；\n");
                prompt.append("   - 技术盲区与短板分析：指明在对应工龄要求下，依然欠缺或回答不充分的技术薄弱点；\n");
                prompt.append("   - 面试官进阶发展建议：给出下一步精准的学习进阶路线与突破方向。\n");
                prompt.append("【输出格式约束】：严格禁止输出任何 Emoji 图标（如 🎯, 💡, 🚀 等），保持排版清爽专业，不要出现无关废话提示。");
            } else {
                prompt.append("【指令：推进到第 ").append(step + 1).append(" 轮深度考核（严格遵循一问一答、引导启发与考题解析）】\n");
                if (CollectionUtils.isEmpty(keywords)) {
                    prompt.append("请结合前述候选人的简历项目经验与作答展开。\n");
                } else {
                    prompt.append("候选人核心技术栈包括：").append(String.join("、", keywords)).append("。\n");
                }
                prompt.append("请你以顶级大厂资深技术考官兼导师身份，严格按照以下四个环节执行：\n");
                prompt.append("1. 客观专业点评：以符合候选人【工作年限：").append(yearsExp).append("】的标准对刚才的回答进行客观评价，明确肯定回答中的亮点与核心得分点，同时指出遗漏的边界条件、概念模糊处或技术偏差；\n");
                prompt.append("2. 帮面试者深度解析该面试题（考点透析）：针对刚才考察的那道技术题，给出工业界权威标准解答与底层原理剖析（阐明底层数据结构/源码设计/高并发生产踩坑点/最佳实践），帮助面试者彻底吃透这道经典题；\n");
                prompt.append("3. 导师启发引导：若候选人刚才回答不完整、卡壳或不确定，平易近人地给出思路启发与思考线索（Hint），引导其建立高阶工程思维；\n");
                prompt.append("4. 抛出下一题（严格一问一答）：在点评与解析完毕后，结合刚才的延伸点或平滑转入下一个核心技术维度，严格只提出【1 个新问题】（切记：绝不要一次抛出多道题或自问自答），并邀请候选人作答；\n");
                prompt.append("5. 【格式与口吻铁律】：\n");
                prompt.append("   - 严禁输出任何 Emoji 表情图标（如 🎯、💡、🚀、📌、👏 等）；\n");
                prompt.append("   - 严禁输出流程化机械标签（如严禁出现“【考官简评】”、“【考题解析】”、“【启发引导】”、“【深度追问】”、“【第").append(step + 1).append("题】”等机械标签）；\n");
                prompt.append("   - 可采用标准的 Markdown 加粗文本或小标题（如 ### 点评与考题深度解析、### 下一题考察）自然组织，语言连贯流利。");
            }

            String reply = sanitizeInterviewReply(service.interviewChat(req.getMemoryId(), prompt.toString()));

            Map<String, Object> data = new HashMap<>();
            data.put("memoryId", req.getMemoryId());
            data.put("currentStep", step + 1);
            data.put("totalSteps", total);
            data.put("jobRole", jobRole);
            data.put("yearsExp", yearsExp);
            data.put("experienceLevel", level);
            data.put("reply", reply);
            data.put("isFinished", isFinish);
            return CommonResult.ok(data);
        } catch (Exception e) {
            log.error("AI 模拟面试交互失败", e);
            return CommonResult.fail("面试交互异常: " + e.getMessage());
        }
    }

    /**
     * 2. SSE 流式 Agent 工具出题接口：实时向前端推送 Agent 思考、工具调用与题目生成结果
     */
    @PostMapping(value = "/generate-questions-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> generateQuestionsStream(@RequestBody AgentInterviewReq req) {
        if (req == null || CollectionUtils.isEmpty(req.getKeywords())) {
            return Flux.just(ServerSentEvent.<String>builder().data("错误：简历提炼关键词不能为空").build());
        }

        AiCodeHelperService service = req.getModelConfig() != null
                ? aiServiceFactory.createService(req.getModelConfig())
                : defaultAiService;

        String jobRole = resolveJobRole(req.getJobRole(), null, null, req.getKeywords());
        String yearsExp = resolveYearsExp(req.getYearsExp(), null, null);
        String level = getSeniorityLevel(yearsExp);

        String prompt = buildAgentPrompt(req, jobRole, yearsExp, level);
        return service.generateQuestionsByAgentStream(prompt)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build())
                .onErrorResume(err -> {
                    log.error("流式出题发生错误", err);
                    return Flux.just(ServerSentEvent.<String>builder()
                            .data("[出题中断: " + err.getMessage() + "]")
                            .build());
                });
    }

    /**
     * 3. 模型连通性测试接口：一键验证配置的 API Key 模式、Gemini CLI 模式或第三方 API 模式
     */
    @PostMapping("/test-connection")
    public CommonResult<Map<String, Object>> testConnection(@RequestBody(required = false) AiModelConfigDto config) {
        long start = System.currentTimeMillis();
        try {
            ChatModel chatModel = dynamicChatModelFactory.createChatModel(config);
            String reply = chatModel.chat("Hello! 请回复'OK'确认连通正常。");
            long cost = System.currentTimeMillis() - start;

            Map<String, Object> data = new HashMap<>();
            data.put("status", "SUCCESS");
            data.put("reply", reply);
            data.put("costMs", cost);
            data.put("mode", config != null && config.getMode() != null ? config.getMode().name() : "DEFAULT");
            return CommonResult.ok(data);
        } catch (Exception e) {
            log.error("AI 模型连通性测试失败", e);
            return CommonResult.fail("连通性测试失败: " + e.getMessage());
        }
    }

    /**
     * 4. 简历文件直传解析端点 (无需 MinIO 即可直接解析 PDF/DOCX 简历中的技能专长与核心关键词)
     */
    @PostMapping(value = "/analyse-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResult<Map<String, Object>> analyseResumeFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return CommonResult.fail("上传简历文件不能为空！");
            }
            log.info("AI 模块接收到直传简历解析请求: 文件名={}, 大小={}", file.getOriginalFilename(), file.getSize());
            String resumeText;
            try {
                resumeText = TIKA.parseToString(file.getInputStream());
            } catch (Exception e) {
                log.error("Tika 解析简历文本失败", e);
                return CommonResult.fail("读取简历文件失败: " + e.getMessage());
            }

            if (!StringUtils.hasText(resumeText)) {
                return CommonResult.fail("未能提取到简历有效文本，请确认文件是否损坏。");
            }

            String cleanResumeText = resumeText.replaceAll("\r", "").replaceAll("\n{3,}", "\n\n").trim();
            Map<String, Object> profile = new HashMap<>();
            List<String> keywords = new ArrayList<>();

            // 智能提取结构化候选人画像（姓名、专业岗位方向、工作年限、核心项目）与纯粹技术关键词
            try {
                ChatModel chatModel = dynamicChatModelFactory.createChatModel(null);
                String prompt = """
                        请仔细分析以下技术简历文本，提取结构化候选人信息。请务必准确识别其【专业岗位方向】和【工作经验年限】，严格以 JSON 格式输出，不要输出任何额外解释或 Markdown 标签：
                        {
                          "name": "候选人姓名（未提及填'同学'）",
                          "jobRole": "专业岗位方向（如: Java 后端开发、Web 前端、移动端开发、高并发系统架构、大数据与数仓、测试与质量保障、运维与DevOps、人工智能与大模型）",
                          "yearsExp": "工作经验年限（如: 应届生、1年经验、3年经验、5年经验、8年经验）",
                          "projects": ["核心项目名称1", "核心项目名称2"],
                          "keywords": ["技术关键词1", "技术关键词2", "技术关键词3"]
                        }
                        
                        简历内容如下：
                        """ + (cleanResumeText.length() > 3000 ? cleanResumeText.substring(0, 3000) : cleanResumeText);

                String reply = chatModel.chat(prompt);
                int startIdx = reply.indexOf("{");
                int endIdx = reply.lastIndexOf("}");
                if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                    String jsonStr = reply.substring(startIdx, endIdx + 1);
                    com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(jsonStr);
                    if (rootNode.has("name")) profile.put("name", rootNode.get("name").asText());
                    if (rootNode.has("jobRole")) profile.put("jobRole", rootNode.get("jobRole").asText());
                    if (rootNode.has("yearsExp")) profile.put("yearsExp", rootNode.get("yearsExp").asText());
                    if (rootNode.has("projects") && rootNode.get("projects").isArray()) {
                        List<String> projList = new ArrayList<>();
                        rootNode.get("projects").forEach(p -> projList.add(p.asText()));
                        profile.put("projects", projList);
                    }
                    if (rootNode.has("keywords") && rootNode.get("keywords").isArray()) {
                        rootNode.get("keywords").forEach(k -> {
                            String kw = k.asText().replaceAll("[`*#1-9.\\\\]", "").trim();
                            if (StringUtils.hasText(kw) && kw.length() <= 25 && !keywords.contains(kw)) {
                                keywords.add(kw);
                            }
                        });
                    }
                }
            } catch (Exception e) {
                log.warn("结构化档案画像提取异常，启用安全规则提取: {}", e.getMessage());
            }

            // 安全兜底：若关键词提取不足，从文本中按词表匹配纯技术关键词
            if (keywords.isEmpty()) {
                List<String> defaultDict = List.of(
                        "Java", "Spring Boot", "Spring Cloud", "MyBatis-Plus", "Redis", "MySQL",
                        "PostgreSQL", "Kingbase", "Kafka", "RabbitMQ", "RocketMQ", "Dubbo",
                        "Docker", "Kubernetes", "MQTT", "WebSocket", "SM2/SM4", "JVM", "Linux"
                );
                String upper = cleanResumeText.toUpperCase();
                for (String word : defaultDict) {
                    if (upper.contains(word.toUpperCase()) && !keywords.contains(word)) {
                        keywords.add(word);
                    }
                    if (keywords.size() >= 8) break;
                }
            }

            String jobRole = resolveJobRole(null, profile, cleanResumeText, keywords);
            String yearsExp = resolveYearsExp(null, profile, cleanResumeText);
            String level = getSeniorityLevel(yearsExp);
            profile.putIfAbsent("jobRole", jobRole);
            profile.putIfAbsent("yearsExp", yearsExp);
            profile.put("experienceLevel", level);

            Map<String, Object> data = new HashMap<>();
            data.put("fileName", file.getOriginalFilename());
            data.put("profile", profile); // 会话级候选人档案元信息
            data.put("jobRole", jobRole); // 专业岗位方向
            data.put("yearsExp", yearsExp); // 工作年限/工龄
            data.put("experienceLevel", level); // 经验梯次
            data.put("keywords", keywords); // 纯粹的技术关键词标签
            data.put("resumeText", cleanResumeText); // 会话知识库原文
            data.put("questionList", keywords.stream().map(k -> Map.of("keyWord", k, "labelName", k)).toList());
            return CommonResult.ok(data);
        } catch (Exception e) {
            log.error("直传解析简历文件失败", e);
            return CommonResult.fail("简历解析异常: " + e.getMessage());
        }
    }

    private String buildAgentPrompt(AgentInterviewReq req, String jobRole, String yearsExp, String level) {
        StringBuilder sb = new StringBuilder();
        sb.append("【候选人画像】：\n");
        sb.append("- 专业方向：").append(jobRole).append("\n");
        sb.append("- 工作年限/工龄：").append(yearsExp).append(" (判定层级：").append(level).append(")\n");
        sb.append("- 核心技能专长关键词：\n");
        for (String kw : req.getKeywords()) {
            sb.append("  * ").append(kw).append("\n");
        }

        sb.append("\n【资深考官出题守则（按工龄与专业梯度合理出题）】：\n");
        sb.append(buildInterviewEvaluationStandard(jobRole, yearsExp, level));
        sb.append("\n【出题执行要求】：\n");
        sb.append("1. 请针对上述每个技能关键词，立即调用你的面试题检索工具 `interviewQuestionSearch` 获取权威真实的真题；\n");
        sb.append("2. 根据工具检索到的题目，精选出与候选人【").append(jobRole).append("】和【").append(yearsExp)
                .append("】难度梯度严格契合的代表性考题（拒绝与工龄严重脱节的超纲难题或低级水题）；\n");
        sb.append("3. 为候选人输出完整的考题内容、考核考察维度以及深度参考解答解析；\n");
        sb.append("4. 严格禁止输出任何 Emoji 图标（如 🎯, 💡, 🚀, 📌, 👏 等），保持排版严谨干净。");
        return sb.toString();
    }

    /**
     * 辅助方法：过滤并清洗 AI 输出，彻底移除 Emoji 图标及残留的机械流程化标签
     */
    private String sanitizeInterviewReply(String reply) {
        if (!StringUtils.hasText(reply)) {
            return reply;
        }
        // 1. 过滤所有 Emoji 表情符号及特殊符号图形 (涵盖常见表情范围与符号标点)
        String cleaned = reply.replaceAll("[\\p{So}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF]", "");
        // 2. 过滤常见行首机械标签（如 【亲切开场】、【考官简评】、【考官专业简评】、【推进考察】、【深度追问】、【第1题】等）
        cleaned = cleaned.replaceAll("(?m)^\\s*(?:\\d+[.、])?\\s*【(?:亲切开场|考官简评|考官专业简评|考官点评|推进考察|阶梯推进与深度追问|深度追问|核心问题|第\\d+题|提出第\\d+题|模拟面试开始|指令|开场白|本轮考察)】[：:]?\\s*", "");
        // 3. 去除多余连续空行
        cleaned = cleaned.replaceAll("\n{3,}", "\n\n").trim();
        return cleaned;
    }

    /**
     * 辅助方法：解析候选人姓名
     */
    private String resolveCandidateName(Map<String, Object> profile) {
        if (profile != null && profile.get("name") != null) {
            String name = String.valueOf(profile.get("name")).trim();
            if (StringUtils.hasText(name) && !"同学".equals(name) && !"候选人".equals(name)) {
                return name;
            }
        }
        return "候选人同学";
    }

    /**
     * 辅助方法：解析专业方向岗位
     */
    private String resolveJobRole(String reqJobRole, Map<String, Object> profile, String resumeText, List<String> keywords) {
        if (StringUtils.hasText(reqJobRole)) {
            return reqJobRole.trim();
        }
        if (profile != null && profile.get("jobRole") != null) {
            String jr = String.valueOf(profile.get("jobRole")).trim();
            if (StringUtils.hasText(jr)) {
                return jr;
            }
        }
        if (StringUtils.hasText(resumeText)) {
            String text = resumeText.toLowerCase();
            if (text.contains("前端") || text.contains("frontend") || text.contains("vue") || text.contains("react")) {
                return "Web 前端开发工程师";
            }
            if (text.contains("算法") || text.contains("深度学习") || text.contains("大模型") || text.contains("llm")) {
                return "人工智能与大模型工程师";
            }
            if (text.contains("大数据") || text.contains("flink") || text.contains("spark") || text.contains("数仓")) {
                return "大数据与数仓工程师";
            }
            if (text.contains("测试") || text.contains("qa") || text.contains("测开")) {
                return "测试与质量保障工程师";
            }
            if (text.contains("运维") || text.contains("devops") || text.contains("sre") || text.contains("k8s")) {
                return "运维与 DevOps 工程师";
            }
        }
        if (!CollectionUtils.isEmpty(keywords)) {
            for (String kw : keywords) {
                String k = kw.toLowerCase();
                if (k.contains("vue") || k.contains("react") || k.contains("typescript") || k.contains("css")) {
                    return "Web 前端开发工程师";
                }
            }
        }
        return "Java 后端开发工程师";
    }

    /**
     * 辅助方法：解析工作年限（工龄）
     */
    private String resolveYearsExp(String reqYearsExp, Map<String, Object> profile, String resumeText) {
        if (StringUtils.hasText(reqYearsExp)) {
            return reqYearsExp.trim();
        }
        if (profile != null && profile.get("yearsExp") != null) {
            String exp = String.valueOf(profile.get("yearsExp")).trim();
            if (StringUtils.hasText(exp)) {
                return exp;
            }
        }
        if (StringUtils.hasText(resumeText)) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)\\s*年(?:工作)?经验|应届生|在校生|实习生");
            java.util.regex.Matcher m = p.matcher(resumeText);
            if (m.find()) {
                return m.group();
            }
        }
        return "1-3年经验";
    }

    /**
     * 辅助方法：根据工龄描述映射到统一经验梯次
     */
    private String getSeniorityLevel(String yearsExp) {
        if (!StringUtils.hasText(yearsExp)) {
            return "1~3年工作经验 (中级研发)";
        }
        String lower = yearsExp.toLowerCase();
        if (lower.contains("应届") || lower.contains("实习") || lower.contains("在校") || lower.contains("0年") || lower.contains("半年") || lower.contains("1年以") || lower.equals("1年") || lower.equals("1年经验")) {
            return "校招/应届/1年以内 (初级研发)";
        }
        if (lower.contains("2年") || lower.contains("3年") || lower.contains("1-3") || lower.contains("1~3")) {
            return "1~3年工作经验 (中级研发)";
        }
        if (lower.contains("4年") || lower.contains("5年") || lower.contains("3-5") || lower.contains("3~5")) {
            return "3~5年工作经验 (高级研发/核心骨干)";
        }
        if (lower.contains("6年") || lower.contains("7年") || lower.contains("8年") || lower.contains("10年") || lower.contains("5年以") || lower.contains("资深") || lower.contains("专家") || lower.contains("架构")) {
            return "5年以上/资深架构师 (技术专家/架构师)";
        }
        return "1~3年工作经验 (中级研发)";
    }

    /**
     * 辅助方法：根据专业方向与工龄梯度构建标准面试官考核指导手册
     */
    private String buildInterviewEvaluationStandard(String jobRole, String yearsExp, String level) {
        StringBuilder sb = new StringBuilder();
        sb.append("【资深大厂考官考核指导手册（因人施测、科学对标）】：\n");
        sb.append("当前考察对象设定为：【应聘方向：").append(jobRole).append("】，【经验层级：").append(yearsExp).append(" (").append(level).append(")】。\n\n");

        sb.append("一、本场面试考题难度标尺与考察重点：\n");
        if (level.contains("初级研发") || level.contains("应届")) {
            sb.append("  - 【考核定位】：重点考核专业基本功、基础概念理解的清晰度、代码编写规范与学习潜力；\n");
            sb.append("  - 【核心考点】：语言核心语法机制、常用API/集合原理、计算机网络基础（HTTP/TCP状态与报文）、操作系统基础（进程线程/基础调度）、标准框架用法与简单CRUD；\n");
            sb.append("  - 【考核禁忌】：严禁提出脱离实际经验的高阶难题（如千万级高并发秒杀调优、跨机房容灾设计、复杂线上故障排查等），注重基础是否扎实。");
        } else if (level.contains("中级研发")) {
            sb.append("  - 【考核定位】：重点考核独立业务模块研发实战、主流框架原理、常用中间件落地与日常业务踩坑排障；\n");
            sb.append("  - 【核心考点】：数据库索引底层与慢SQL优化、Redis常见数据结构选型与缓存三大问题（击穿/穿透/雪崩）、Spring生命周期与事务传播特性、多线程基础与线程池参数配置；\n");
            sb.append("  - 【考核要求】：注重工程落地能力、代码健壮性、以及日常开发中的排错经验与原理理解，知其然并开始理解其所以然。");
        } else if (level.contains("高级研发") || level.contains("核心骨干")) {
            sb.append("  - 【考核定位】：重点考核底层源码机制、高并发与微服务分布式架构治理、复杂线上生产事故排查与全链路调优；\n");
            sb.append("  - 【核心考点】：深入底层机制（AQS/并发锁机制源码、JVM内存模型与GC调优实战、MySQL MVCC与死锁排查、MQ消息可靠投递与积压治理）、微服务高可用（分布式锁、分布式事务、限流熔断降级）、复杂线上Bug排障（OOM、CPU 100%）；\n");
            sb.append("  - 【考核要求】：知其然更知其所以然，考察高并发极限场景与生产排障能力。");
        } else {
            sb.append("  - 【考核定位】：重点考核系统级全局架构设计、领域驱动设计（DDD）、全链路压测与容量规划、系统容灾多活、中间件内核与技术选型权衡 (Trade-off)；\n");
            sb.append("  - 【核心考点】：大规模高可用分布式系统架构设计、技术选型演进与架构权衡、数据最终一致性方案、多级缓存与流量削峰、团队技术攻坚与复杂业务建模；\n");
            sb.append("  - 【考核要求】：由点及面，从微观实现升华到宏观架构设计与方案演进，考察全局架构视野与系统级思考。");
        }

        sb.append("\n\n二、专业方向【").append(jobRole).append("】核心考核主线（遵循平台三级分类规范）：\n");
        String roleLower = jobRole.toLowerCase();
        if (roleLower.contains("前端") || roleLower.contains("web") || roleLower.contains("front")) {
            sb.append("  - 聚焦：JavaScript/TypeScript 异步事件循环、闭包原型链、Vue3/React 响应式与Diff算法、前端工程化与首屏渲染性能调优、跨端与安全防护。\n");
        } else if (roleLower.contains("算法") || roleLower.contains("人工智能") || roleLower.contains("大模型") || roleLower.contains("llm")) {
            sb.append("  - 聚焦：大模型训练与微调、Prompt 架构、RAG 向量检索召回、Agent 工具调用与规划、量化推理与评估基准。\n");
        } else if (roleLower.contains("大数据") || roleLower.contains("数仓") || roleLower.contains("data")) {
            sb.append("  - 聚焦：Flink/Spark 实时流批计算引擎、分布式存储（HDFS/HBase/ClickHouse）、数仓分层设计与数据倾斜调优。\n");
        } else if (roleLower.contains("测试") || roleLower.contains("qa")) {
            sb.append("  - 聚焦：自动化测试框架、接口与契约测试、性能压测指标剖析、全链路监控与质量左移体系。\n");
        } else if (roleLower.contains("运维") || roleLower.contains("devops") || roleLower.contains("sre")) {
            sb.append("  - 聚焦：K8s 容器编排、CI/CD 自动化流水线、Prometheus 可观测性体系、网络排障与系统容灾。\n");
        } else {
            sb.append("  - 聚焦：语言核心特性与并发安全、JVM/运行时、MySQL 索引与事务机制、Redis 缓存与 MQ 消息队列、微服务治理与高可用分布式设计。\n");
        }

        sb.append("三、面试官提问与追问执行守则：\n");
        sb.append("  1. 必须精准匹配当前候选人的【").append(yearsExp).append("】经验深度出题，杜绝低工龄超纲，杜绝高工龄水题；\n");
        sb.append("  2. 严禁死记硬背脱离实际的八股文，必须紧扣候选人真实项目经历、落地场景或技术选型展开；\n");
        sb.append("  3. 严格遵循一问一答，单次交互严格只提 1 个核心技术问题，并结合回答进行梯次递进点评与追问。");

        return sb.toString();
    }
}
