package com.suifeng.server.ai;

import com.suifeng.server.memory.ChatMemoryProvider;
import com.suifeng.server.tools.InterviewQuestionTool;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeHelperServiceFactory {

    @Resource
    private ChatModel myQwenChatModel;

    @Resource
    private ContentRetriever contentRetriever;

    @Resource
    private McpToolProvider mcpToolProvider;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    @Resource
    private ChatMemoryProvider chatMemoryProvider;

    @Bean
    public AiCodeHelperService aiCodeHelperService(ToolProvider toolProvider){
        //弃用内存的会话记忆：每用户最多保留10条
//        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.withMaxMessages(10);
        //构造Ai Service
        AiCodeHelperService aiCodeHelpService = AiServices
                .builder(AiCodeHelperService.class)
                .chatModel(myQwenChatModel)
                .streamingChatModel(qwenStreamingChatModel) //流式输出
                .chatMemoryProvider(chatMemoryProvider::get) //每个会话独立存储，最多保留10条上下文
                .contentRetriever(contentRetriever)  //RAG 检索增强生成
                .tools(new InterviewQuestionTool()) //工具调用
                .toolProvider(mcpToolProvider) //mcp工具调用
                .build();
        return aiCodeHelpService;
    }
}

