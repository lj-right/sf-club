package com.suifeng.server.model;

/**
 * AI 模型接入模式
 */
public enum AiModelMode {
    /**
     * 1. API Key 方式：直连官方云端服务 (如 DashScope 通义千问)，只需 apiKey 与 modelName
     */
    API_KEY,

    /**
     * 2. 第三方 API 方式：接入第三方聚合/代理/自建网关，支持自定义 baseUrl + apiKey + modelName
     */
    THIRD_PARTY,

    /**
     * 3. 第三方 CLI 方式：支持接入本地/第三方 CLI 命令行进程或 CLI 启动的本地模型服务 (如 Ollama CLI、本地命令行模型)
     */
    CLI
}
