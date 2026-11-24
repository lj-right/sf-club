package com.suifeng.server.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 模型配置属性：支持官方 API Key 模式、第三方 API 模式以及第三方 CLI 命令行模式
 */
@Component
@ConfigurationProperties(prefix = "ai.model")
@Data
public class AiModelProperties {

    /**
     * 接入模式：API_KEY (官方直连) | THIRD_PARTY (第三方 API 代理) | CLI (第三方 CLI / 本地命令行)
     */
    private AiModelMode mode = AiModelMode.API_KEY;

    /**
     * 模型名称，如 qwen-plus, qwen-turbo, deepseek-chat, qwen:7b 等
     */
    private String modelName = "qwen-plus";

    /**
     * API 密钥 (用于官方 API Key 模式或第三方 API 鉴权，CLI 模式可留空)
     */
    private String apiKey = "sk-demo-key";

    /**
     * 第三方 API 根地址 (如第三方中转站地址)
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    /**
     * 第三方 CLI 命令行执行程序 (当 mode 为 CLI 时生效，例如 "ollama" 或自定义 cli 脚本路径)
     */
    private String cliCommand = "ollama";

    /**
     * 第三方 CLI 服务本地端点 (如 "http://localhost:11434/v1")
     */
    private String cliEndpoint = "http://localhost:11434/v1";

    /**
     * 请求超时时间 (秒)
     */
    private Integer timeoutSeconds = 60;

    /**
     * 采样温度 (0.0 ~ 2.0)
     */
    private Double temperature = 0.7;
}
