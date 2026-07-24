package com.suifeng.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Langchain4jServerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Langchain4jServerApplication.class, args);
//        AiCodeHelper aiCodeHelper = (AiCodeHelper)context.getBean(AiCodeHelper.class);
//        aiCodeHelper.chat("你好,我是随风！");

//        AiCodeHelper aiCodeHelper = context.getBean(AiCodeHelper.class);
//        aiCodeHelper.chatWithMessage(UserMessage.from(
//                TextContent.from("今天出太阳了,我和我的好朋友出门了，这是它的图片"),
//                ImageContent.from("https://loremflickr.com/320/240/dog")
//        ));

    }

}
