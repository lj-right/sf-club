package com.suifeng.sfclublangchain4jserver.ai;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

//@AiService
public interface AiCodeHelperService {
    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(@MemoryId int memoryId,@UserMessage String userMsg);


    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(@MemoryId int memoryId,@UserMessage String userMsg);

    record Report(String name, List<String> suggestionList){};
}
