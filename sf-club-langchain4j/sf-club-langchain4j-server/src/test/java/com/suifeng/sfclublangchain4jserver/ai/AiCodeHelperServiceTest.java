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
        String resultMeassage = aiCodeHelperService.chat("你好，我是随风");
        System.out.println(resultMeassage);
    }

    @Test
    void chatWithMessage() {
        String result = aiCodeHelperService.chat("你好，我是随风");
        System.out.println(result);
        result = aiCodeHelperService.chat("我是谁来着？小民吗？");
        System.out.println(result);
    }
}