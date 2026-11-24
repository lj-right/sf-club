package com.suifeng.server.controller.dto;

import com.suifeng.server.model.AiModelConfigDto;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Agent 出题请求入参
 */
@Data
public class AgentInterviewReq implements Serializable {

    /**
     * 简历分析提取出的技术关键词列表 (如: ["Redis", "Spring Boot", "MySQL", "多线程"])
     */
    private List<String> keywords;

    /**
     * 候选人岗位或意向方向 (可选，如 "Java 后端开发", "Web 前端", "高并发系统架构")
     */
    private String jobRole;

    /**
     * 候选人工作年限/年龄 (可选，如 "应届生", "1-3年", "3-5年", "5年以上", 或具体的 "3年")
     */
    private String yearsExp;

    /**
     * 候选人经验层级 (可选，如 "初级研发", "中级研发", "高级研发", "资深架构师")
     */
    private String experienceLevel;

    /**
     * 自定义大模型接入配置 (可选：支持 API_KEY 官方直连或 THIRD_PARTY 第三方 API 两种方式)
     */
    private AiModelConfigDto modelConfig;
}
