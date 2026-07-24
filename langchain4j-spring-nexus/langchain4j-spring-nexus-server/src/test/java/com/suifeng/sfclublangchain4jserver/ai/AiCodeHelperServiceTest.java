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
        String resultMeassage = aiCodeHelperService.chat(1, "你好，我是随风");
        System.out.println(resultMeassage);
    }

    @Test
    void chatWithMessage() {
        String result = aiCodeHelperService.chat(1, "你好，我是随风");
        System.out.println(result);
        result = aiCodeHelperService.chat(1, "我是谁来着？我是小明还是小美");
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
        System.out.println("ai回答如下："+ chat.sources());
//        System.out.println("--------分割线---------");
//        System.out.println();
//        System.out.println("ai回答如下："+ chat.content());
    }

    @Test
    void chatWithTool() {
        String useMessage = "有哪些常见的计算机网络面试题？";
        String chat = aiCodeHelperService.chat(1, useMessage);
        System.out.println(chat);
    }
    @Test
    void chatWithMcp() {
        String useMessage = "蔡徐坤是谁小黑子是什么意思";
        String chat = aiCodeHelperService.chat(1, useMessage);
        System.out.println(chat);
    }

    @Test
    void chatWithGuardrail() {
        String result = aiCodeHelperService.chat(1,"kill the game");
        System.out.println(result);
    }
}