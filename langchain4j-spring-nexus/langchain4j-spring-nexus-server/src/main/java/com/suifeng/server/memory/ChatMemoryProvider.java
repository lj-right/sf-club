package com.suifeng.server.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.stereotype.Component;

@Component
public class ChatMemoryProvider {

    private final PersistentChatMemoryStore persistentChatMemoryStore;

    public ChatMemoryProvider(PersistentChatMemoryStore persistentChatMemoryStore) {
        this.persistentChatMemoryStore = persistentChatMemoryStore;
    }

    public ChatMemory get(Object memoryId) {
        return MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(ChatMemoryService.MAX_CONTEXT_MESSAGES)
                .chatMemoryStore(persistentChatMemoryStore)
                .build();
    }
}
