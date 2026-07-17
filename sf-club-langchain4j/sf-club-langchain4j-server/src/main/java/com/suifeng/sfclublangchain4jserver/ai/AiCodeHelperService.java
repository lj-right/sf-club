package com.suifeng.sfclublangchain4jserver.ai;


import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

//@AiService
public interface AiCodeHelperService {
    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(String userMsg);
}
