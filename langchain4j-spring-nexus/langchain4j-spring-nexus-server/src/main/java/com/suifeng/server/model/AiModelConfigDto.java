package com.suifeng.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 动态模型配置入参 DTO (支持客户端按需指定 API Key 方式、第三方 API 方式或第三方 CLI 方式)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModelConfigDto implements Serializable {

    /**
     * 接入模式：API_KEY / THIRD_PARTY / CLI
     */
    private AiModelMode mode;

    /**
     * API 密钥
     */
    private String apiKey;

    /**
     * 第三方 API 根地址 (如 https://api.deepseek.com/v1)
     */
    private String baseUrl;

    /**
     * 模型名称 (如 qwen-plus, deepseek-chat, qwen:7b)
     */
    private String modelName;

    /**
     * 第三方 CLI 命令行程序或路径 (如 "ollama" 或自定义 cli)
     */
    private String cliCommand;

    /**
     * 第三方 CLI 本地服务端口/URL (如 "http://localhost:11434/v1")
     */
    private String cliEndpoint;

    /**
     * 采样温度
     */
    private Double temperature;
}
