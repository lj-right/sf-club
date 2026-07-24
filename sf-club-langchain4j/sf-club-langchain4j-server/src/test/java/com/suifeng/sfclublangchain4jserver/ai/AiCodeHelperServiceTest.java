package com.suifeng.sfclublangchain4jserver.ai;

import com.suifeng.server.ai.AiCodeHelperService;
import dev.langchain4j.service.Result;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiCodeHelperServiceTest {


    @Resource
    private AiCodeHelperService aiCodeHelperService;

    @Test
    void chat() {
        String resultMeassage = aiCodeHelperService.chat(1,"你好，我是随风");
        System.out.println(resultMeassage);
    }

    @Test
    void chatWithMessage() {
        String result = aiCodeHelperService.chat(1,"你好，我是随风");
        System.out.println(result);
        result = aiCodeHelperService.chat(1,"我是谁来着？我是小明还是小美");
        System.out.println(result);
    }

    @Test
    void chatForReport() {
        String useMessage = "你好，我是随风，喜欢唱跳rap篮球，请帮我制定一周编程刷题计划";
        AiCodeHelperService.Report report = aiCodeHelperService.chatForReport(1, useMessage);
        System.out.println(report);
    }

    @Test
    void chatWithRag() {
        String useMessage = "怎么学习Java，有哪些面试题";
        Result<String> chat = aiCodeHelperService.chatWithRag(1, useMessage);
        System.out.println(chat.sources());
        System.out.println(chat.content());
    }
    @Test
    void chatWithTools() {
        String result = aiCodeHelperService.chat(1,"有哪些常见的计算机网络面试题？");
        System.out.println(result);
    }

    @Test
    void chatWithMcp() {
        String result = aiCodeHelperService.chat(1,"什么是程序员鱼皮的编程导航？");
        System.out.println(result);
    }

    @Test
    void chatWithGuardrail() {
        String result = aiCodeHelperService.chat(1,"kill the game");
        System.out.println(result);
    }
}