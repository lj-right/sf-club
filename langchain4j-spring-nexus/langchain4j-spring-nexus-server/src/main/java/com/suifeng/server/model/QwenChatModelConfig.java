package com.suifeng.server.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 兼容旧版 DashScope 配置属性 (已升级至 DynamicChatModelFactory 统一管理)
 */
@Component
@ConfigurationProperties(prefix = "langchain4j.community.dashscope.chat-model")
@Data
public class QwenChatModelConfig {
    private String modelName;
    private String apiKey;
}
