package com.suifeng.sfclublangchain4jserver.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiCodeHelper {

    @Resource
    private ChatModel qwenChatModel;

    private static final String SYSTEM_MESSAGE = """
            你是专业刷题助手，支持出题、解题、考点总结：
            1. 按知识点/难度出题，单次最多3道；
            2. 分步解题，标注易错点；
            3. 讲完提炼解题模板，询问是否要变式练习。
            输出分【题目】【解答】【总结】三块，简洁清晰。
                """;


    public String chat(String msg){
        SystemMessage systemMessage = SystemMessage.from(SYSTEM_MESSAGE);
        UserMessage userMessage = UserMessage.from(msg);
        ChatResponse chatResponse = qwenChatModel.chat(systemMessage,userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("ai 输出：" + aiMessage.toString());
        return aiMessage.text();
    }
    //多模态的简单对话
    public String chatWithMessage(UserMessage userMessage){
        ChatResponse chatResponse = qwenChatModel.chat(userMessage);
        AiMessage aiMessage = chatResponse.aiMessage();
        log.info("ai 输出：" + aiMessage.toString());
        return aiMessage.text();
    }

}
