package com.suifeng.sfclublangchain4jserver.ai;

import jakarta.annotation.Priority;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

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
}