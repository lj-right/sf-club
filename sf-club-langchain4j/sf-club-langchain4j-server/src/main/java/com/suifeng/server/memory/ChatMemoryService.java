package com.suifeng.server.memory;

import com.suifeng.server.memory.Entity.ChatMemoryDto;
import com.suifeng.server.memory.Entity.ChatMemoryRecord;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMemoryService {

    public static final int MAX_CONTEXT_MESSAGES = 10;

    private final ChatMemoryRepository chatMemoryRepository;

    public ChatMemoryService(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
    }

    public ChatMemoryDto getById(Long memoryId) {
        ChatMemoryRecord record = chatMemoryRepository.findById(memoryId)
                .orElseThrow(() -> new ChatMemoryNotFoundException(memoryId));
        return toDto(record, record.getMessages());
    }

    public List<ChatMemoryDto> listAll() {
        return chatMemoryRepository.findAllOrderByUpdateAtDesc().stream()
                .map(record -> toDto(record, limitMessages(record.getMessages(), MAX_CONTEXT_MESSAGES)))
                .toList();
    }

    public ChatMemoryDto upsert(Long memoryId, String messagesJson) {
        chatMemoryRepository.upsert(memoryId, messagesJson);
        return getById(memoryId);
    }

    public void delete(Long memoryId) {
        if (!chatMemoryRepository.softDeleteById(memoryId)) {
            throw new ChatMemoryNotFoundException(memoryId);
        }
    }

    public String limitMessages(String messagesJson, int maxMessages) {
        if (messagesJson == null || messagesJson.isBlank()) {
            return "[]";
        }
        List<ChatMessage> messages = ChatMessageDeserializer.messagesFromJson(messagesJson);
        if (messages.size() <= maxMessages) {
            return messagesJson;
        }
        return ChatMessageSerializer.messagesToJson(
                messages.subList(messages.size() - maxMessages, messages.size())
        );
    }

    private ChatMemoryDto toDto(ChatMemoryRecord record, String messages) {
        return ChatMemoryDto.builder()
                .memoryId(record.getMemoryId())
                .messages(messages == null || messages.isBlank() ? "[]" : messages)
                .createAt(record.getCreateAt())
                .updateAt(record.getUpdateAt())
                .isDeleted(record.getIsDeleted())
                .build();
    }

    public static class ChatMemoryNotFoundException extends RuntimeException {

        public ChatMemoryNotFoundException(Long memoryId) {
            super("Chat memory not found: " + memoryId);
        }
    }
}
