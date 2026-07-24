package com.suifeng.server.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryRepository chatMemoryRepository;

    public PersistentChatMemoryStore(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return chatMemoryRepository.findById(toMemoryId(memoryId))
                .map(record -> ChatMessageDeserializer.messagesFromJson(record.getMessages()))
                .orElse(Collections.emptyList());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        chatMemoryRepository.upsert(
                toMemoryId(memoryId),
                ChatMessageSerializer.messagesToJson(messages)
        );
    }

    @Override
    public void deleteMessages(Object memoryId) {
        chatMemoryRepository.softDeleteById(toMemoryId(memoryId));
    }

    private Long toMemoryId(Object memoryId) {
        if (memoryId instanceof Number) {
            return ((Number) memoryId).longValue();
        }
        throw new IllegalArgumentException("memoryId must be a number, but was: " + memoryId);
    }
}
