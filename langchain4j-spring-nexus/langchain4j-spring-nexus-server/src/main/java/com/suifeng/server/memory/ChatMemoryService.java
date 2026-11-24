package com.suifeng.server.memory;

import com.suifeng.server.memory.Entity.ChatMemoryDto;
import com.suifeng.server.memory.Entity.ChatMemoryRecord;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ChatMemoryService {

    public static final int MAX_CONTEXT_MESSAGES = 10;

    private final ChatMemoryRepository chatMemoryRepository;
    private final Map<Long, ChatMemoryRecord> memoryMap = new ConcurrentHashMap<>();

    public ChatMemoryService(@Autowired(required = false) ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
        if (chatMemoryRepository == null) {
            log.info("未检测到数据库持久化仓储，ChatMemoryService 启用高性能内存存储模式");
        }
    }

    public ChatMemoryDto getById(Long memoryId) {
        if (chatMemoryRepository != null) {
            ChatMemoryRecord record = chatMemoryRepository.findById(memoryId)
                    .orElseThrow(() -> new ChatMemoryNotFoundException(memoryId));
            return toDto(record, record.getMessages());
        }
        ChatMemoryRecord record = memoryMap.get(memoryId);
        if (record == null || record.getIsDeleted() == 1) {
            throw new ChatMemoryNotFoundException(memoryId);
        }
        return toDto(record, record.getMessages());
    }

    public List<ChatMemoryDto> listAll() {
        if (chatMemoryRepository != null) {
            return chatMemoryRepository.findAllOrderByUpdateAtDesc().stream()
                    .map(record -> toDto(record, limitMessages(record.getMessages(), MAX_CONTEXT_MESSAGES)))
                    .toList();
        }
        return memoryMap.values().stream()
                .filter(r -> r.getIsDeleted() == 0)
                .sorted(Comparator.comparing(ChatMemoryRecord::getUpdateAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(r -> toDto(r, limitMessages(r.getMessages(), MAX_CONTEXT_MESSAGES)))
                .toList();
    }

    public ChatMemoryDto upsert(Long memoryId, String messagesJson) {
        if (chatMemoryRepository != null) {
            chatMemoryRepository.upsert(memoryId, messagesJson);
            return getById(memoryId);
        }
        LocalDateTime now = LocalDateTime.now();
        ChatMemoryRecord record = ChatMemoryRecord.builder()
                .memoryId(memoryId)
                .messages(messagesJson)
                .createAt(now)
                .updateAt(now)
                .isDeleted((short) 0)
                .build();
        memoryMap.put(memoryId, record);
        return toDto(record, record.getMessages());
    }

    public void delete(Long memoryId) {
        if (chatMemoryRepository != null) {
            if (!chatMemoryRepository.softDeleteById(memoryId)) {
                throw new ChatMemoryNotFoundException(memoryId);
            }
            return;
        }
        ChatMemoryRecord record = memoryMap.get(memoryId);
        if (record != null) {
            record.setIsDeleted((short) 1);
        } else {
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
