package com.suifeng.server.ai;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

import java.util.List;

//@AiService
public interface AiCodeHelperService {
    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(@MemoryId int memoryId, @UserMessage String userMsg);


    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(@MemoryId int memoryId, @UserMessage String userMsg);

    record Report(String name, List<String> suggestionList) {
    }

    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatWithRag(@MemoryId int memoryId, @UserMessage String userMessage);

    //流式输出
    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
