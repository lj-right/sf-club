package com.suifeng.sfclublangchain4jserver.ai;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiCodeHelperServiceFactory {

    @Resource
    private ChatModel qwenChatModel;

    @Bean
    public AiCodeHelperService aiCodeHelperService(){
        //会话记忆：每用户最多保留10条
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.withMaxMessages(10);
        //构造Ai Service
        AiCodeHelperService aiCodeHelpService = AiServices
                .builder(AiCodeHelperService.class)
                .chatModel(qwenChatModel)
                .chatMemory(messageWindowChatMemory)
                .build();
        return aiCodeHelpService;
    }
}
