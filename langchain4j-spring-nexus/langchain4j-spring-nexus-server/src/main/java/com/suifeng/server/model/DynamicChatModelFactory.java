package com.suifeng.server.model;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 动态聊天模型工厂：统一支持【Gemini CLI / 第三方 CLI】、【官方 API Key】以及【第三方 API 代理】三种方式
 */
@Configuration
@Slf4j
public class DynamicChatModelFactory {

    @Resource
    private AiModelProperties properties;

    @Autowired(required = false)
    private ChatModelListener chatModelListener;

    /**
     * 默认 ChatModel (主 Bean)
     */
    @Bean(name = {"chatModel", "myQwenChatModel"})
    @Primary
    public ChatModel defaultChatModel() {
        log.info("初始化默认 AI ChatModel -> 模式: {}, 模型: {}", properties.getMode(), properties.getModelName());
        return createChatModel(null);
    }

    /**
     * 默认 StreamingChatModel (主 Bean)
     */
    @Bean(name = {"streamingChatModel", "qwenStreamingChatModel"})
    @Primary
    public StreamingChatModel defaultStreamingChatModel() {
        log.info("初始化默认 AI StreamingChatModel -> 模式: {}, 模型: {}", properties.getMode(), properties.getModelName());
        return createStreamingChatModel(null);
    }

    /**
     * 根据请求配置动态构建 ChatModel
     */
    public ChatModel createChatModel(AiModelConfigDto customConfig) {
        AiModelMode mode = resolveMode(customConfig);
        String apiKey = resolveApiKey(customConfig);
        String modelName = resolveModelName(customConfig, mode);
        Float temperature = resolveTemperature(customConfig);
        int timeout = properties.getTimeoutSeconds() != null ? properties.getTimeoutSeconds() : 90;

        // 1. Gemini CLI 或 第三方 CLI 方式
        if (mode == AiModelMode.CLI) {
            String cliCmd = resolveCliCommand(customConfig);
            String cliEndpoint = resolveCliEndpoint(customConfig);

            // 如果配置了本地 HTTP 服务端点则优先走 HTTP
            if (StringUtils.hasText(cliEndpoint)) {
                log.info("采用【CLI 本地服务端点】构建 ChatModel -> Endpoint: {}, Model: {}", cliEndpoint, modelName);
                var builder = QwenChatModel.builder()
                        .baseUrl(cliEndpoint)
                        .apiKey(StringUtils.hasText(apiKey) ? apiKey : "cli-local-key")
                        .modelName(modelName)
                        .temperature(temperature);
                if (chatModelListener != null) {
                    builder.listeners(List.of(chatModelListener));
                }
                return builder.build();
            } else {
                log.info("采用【Gemini CLI (agy) / 本地命令行】构建 ChatModel -> 命令: {}, 模型: {}", cliCmd, modelName);
                ChatModel fallbackModel = null;
                if (StringUtils.hasText(apiKey)) {
                    try {
                        fallbackModel = QwenChatModel.builder()
                                .apiKey(apiKey)
                                .modelName("qwen-plus")
                                .temperature(temperature)
                                .build();
                    } catch (Exception e) {
                        log.warn("初始化 CLI Fallback 模型失败: {}", e.getMessage());
                    }
                }
                return CliChatModel.builder()
                        .cliCommand(cliCmd)
                        .modelName(modelName)
                        .timeoutSeconds(timeout)
                        .fallbackChatModel(fallbackModel)
                        .build();
            }
        }

        // 2. 第三方 API 代理方式 (支持自定义 BaseURL)
        if (mode == AiModelMode.THIRD_PARTY) {
            String baseUrl = resolveBaseUrl(customConfig);
            log.info("采用【第三方 API 代理方式】构建 ChatModel -> BaseUrl: {}, Model: {}", baseUrl, modelName);
            var builder = QwenChatModel.builder()
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .temperature(temperature);
            if (chatModelListener != null) {
                builder.listeners(List.of(chatModelListener));
            }
            return builder.build();
        }

        // 3. API Key 官方直连方式 (默认 DashScope 通义千问等)
        log.info("采用【API Key 官方直连方式】构建 ChatModel -> Model: {}", modelName);
        var builder = QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature);
        if (chatModelListener != null) {
            builder.listeners(List.of(chatModelListener));
        }
        return builder.build();
    }

    /**
     * 根据请求配置动态构建 StreamingChatModel
     */
    public StreamingChatModel createStreamingChatModel(AiModelConfigDto customConfig) {
        AiModelMode mode = resolveMode(customConfig);
        String apiKey = resolveApiKey(customConfig);
        String modelName = resolveModelName(customConfig, mode);
        Float temperature = resolveTemperature(customConfig);

        // 1. CLI 模式下的流式适配器
        if (mode == AiModelMode.CLI) {
            String cliEndpoint = resolveCliEndpoint(customConfig);
            if (StringUtils.hasText(cliEndpoint)) {
                return QwenStreamingChatModel.builder()
                        .baseUrl(cliEndpoint)
                        .apiKey(StringUtils.hasText(apiKey) ? apiKey : "cli-local-key")
                        .modelName(modelName)
                        .temperature(temperature)
                        .build();
            }

            // 若使用原生 CLI 命令，包装为同步调用并单次流式发射
            final ChatModel cliChatModel = createChatModel(customConfig);
            return new StreamingChatModel() {
                @Override
                public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
                    try {
                        ChatResponse response = cliChatModel.doChat(chatRequest);
                        String text = response.aiMessage() != null ? response.aiMessage().text() : "";
                        handler.onPartialResponse(text);
                        handler.onCompleteResponse(response);
                    } catch (Exception e) {
                        handler.onError(e);
                    }
                }
            };
        }

        // 2. 第三方 API 代理模式
        if (mode == AiModelMode.THIRD_PARTY) {
            String baseUrl = resolveBaseUrl(customConfig);
            log.info("采用【第三方 API 代理方式】构建 StreamingChatModel -> BaseUrl: {}, Model: {}", baseUrl, modelName);
            return QwenStreamingChatModel.builder()
                    .baseUrl(baseUrl)
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .temperature(temperature)
                    .build();
        }

        // 3. 官方直连模式
        log.info("采用【API Key 官方直连方式】构建 StreamingChatModel -> Model: {}", modelName);
        return QwenStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    private AiModelMode resolveMode(AiModelConfigDto customConfig) {
        if (customConfig != null) {
            if (customConfig.getMode() != null) {
                return customConfig.getMode();
            }
            if (StringUtils.hasText(customConfig.getCliCommand())) {
                return AiModelMode.CLI;
            }
            if (StringUtils.hasText(customConfig.getBaseUrl())) {
                return AiModelMode.THIRD_PARTY;
            }
        }
        return properties.getMode() != null ? properties.getMode() : AiModelMode.API_KEY;
    }

    private String resolveApiKey(AiModelConfigDto customConfig) {
        if (customConfig != null && StringUtils.hasText(customConfig.getApiKey())) {
            return customConfig.getApiKey().trim();
        }
        return properties.getApiKey();
    }

    private String resolveBaseUrl(AiModelConfigDto customConfig) {
        if (customConfig != null && StringUtils.hasText(customConfig.getBaseUrl())) {
            return customConfig.getBaseUrl().trim();
        }
        return properties.getBaseUrl();
    }

    private String resolveModelName(AiModelConfigDto customConfig, AiModelMode mode) {
        if (customConfig != null && StringUtils.hasText(customConfig.getModelName())) {
            return customConfig.getModelName().trim();
        }
        if (mode == AiModelMode.CLI) {
            return "gemini-3.8-flash-high";
        }
        return properties.getModelName();
    }

    private String resolveCliCommand(AiModelConfigDto customConfig) {
        if (customConfig != null && StringUtils.hasText(customConfig.getCliCommand())) {
            return customConfig.getCliCommand().trim();
        }
        return StringUtils.hasText(properties.getCliCommand()) ? properties.getCliCommand() : "agy";
    }

    private String resolveCliEndpoint(AiModelConfigDto customConfig) {
        if (customConfig != null && StringUtils.hasText(customConfig.getCliEndpoint())) {
            return customConfig.getCliEndpoint().trim();
        }
        return properties.getCliEndpoint();
    }

    private Float resolveTemperature(AiModelConfigDto customConfig) {
        if (customConfig != null && customConfig.getTemperature() != null) {
            return customConfig.getTemperature().floatValue();
        }
        return properties.getTemperature() != null ? properties.getTemperature().floatValue() : 0.7f;
    }
}
