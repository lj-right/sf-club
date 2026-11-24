package com.suifeng.server.ai;

import com.suifeng.server.memory.ChatMemoryProvider;
import com.suifeng.server.model.AiModelConfigDto;
import com.suifeng.server.model.DynamicChatModelFactory;
import com.suifeng.server.tools.InterviewQuestionTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class AiCodeHelperServiceFactory {

    @Resource
    private DynamicChatModelFactory dynamicChatModelFactory;

    @Autowired(required = false)
    private ContentRetriever contentRetriever;

    @Autowired(required = false)
    private McpToolProvider mcpToolProvider;

    @Autowired(required = false)
    private ChatMemoryProvider chatMemoryProvider;

    @Resource
    private InterviewQuestionTool interviewQuestionTool;

    /**
     * 默认 AiCodeHelperService Bean
     */
    @Bean
    @Primary
    public AiCodeHelperService aiCodeHelperService() {
        return createService(null);
    }

    /**
     * 根据动态配置（第三方 API 方式 / API Key 方式）创建专属的 AiCodeHelperService
     */
    public AiCodeHelperService createService(AiModelConfigDto customConfig) {
        ChatModel chatModel = dynamicChatModelFactory.createChatModel(customConfig);
        StreamingChatModel streamingChatModel = dynamicChatModelFactory.createStreamingChatModel(customConfig);

        var builder = AiServices.builder(AiCodeHelperService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .tools(interviewQuestionTool); // 核心：注入面试题 Agent 工具

        if (chatMemoryProvider != null) {
            builder.chatMemoryProvider(chatMemoryProvider::get);
        }

        if (contentRetriever != null) {
            builder.contentRetriever(contentRetriever);
        }

        if (mcpToolProvider != null) {
            builder.toolProvider(mcpToolProvider);
        }

        return builder.build();
    }
}
