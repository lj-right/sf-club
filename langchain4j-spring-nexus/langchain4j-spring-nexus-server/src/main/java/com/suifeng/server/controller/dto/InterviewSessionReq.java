package com.suifeng.server.controller.dto;

import com.suifeng.server.model.AiModelConfigDto;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 实时面试会话请求入参
 */
@Data
public class InterviewSessionReq implements Serializable {

    /**
     * 面试会话记忆 ID（多轮对话保持一致）
     */
    private String memoryId;

    /**
     * 简历提炼出的核心技能关键词 (如 ["Java", "Redis", "MySQL", "并发"])
     */
    private List<String> keywords;

    /**
     * 候选人岗位方向 (如 "Java 后端开发", "Web 前端", "人工智能与大模型" 等)
     */
    private String jobRole;

    /**
     * 候选人工作年限/年龄 (如 "应届生", "1-3年", "3-5年", "5年以上", 或具体的 "3年")
     */
    private String yearsExp;

    /**
     * 候选人经验层级 (可选，如 "初级研发", "中级研发", "高级研发", "资深架构师")
     */
    private String experienceLevel;

    /**
     * 候选人简历原始提取文本（构建知识库核心上下文）
     */
    private String resumeText;

    /**
     * 候选人简历项目与技术画像摘要
     */
    private String resumeSummary;

    /**
     * 候选人结构化会话档案画像 (姓名、经验、核心项目列表等)
     */
    private java.util.Map<String, Object> profile;

    /**
     * 候选人的回答内容 (开启首轮时可为空)
     */
    private String userAnswer;

    /**
     * 当前面试轮次 (从 1 开始)
     */
    private Integer currentStep = 1;

    /**
     * 总面试轮次 (默认 4 轮)
     */
    private Integer totalSteps = 4;

    /**
     * 是否主动提前结束面试
     */
    private Boolean finish = false;

    /**
     * 模型接入配置 (CLI / API_KEY / THIRD_PARTY)
     */
    private AiModelConfigDto modelConfig;
}
