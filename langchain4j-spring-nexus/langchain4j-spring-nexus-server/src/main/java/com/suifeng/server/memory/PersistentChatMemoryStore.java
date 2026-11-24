package com.suifeng.server.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryRepository chatMemoryRepository;
    private final Map<Long, List<ChatMessage>> inMemoryStorage = new ConcurrentHashMap<>();

    public PersistentChatMemoryStore(@Autowired(required = false) ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
        if (chatMemoryRepository == null) {
            log.info("未检测到数据库持久化仓储，ChatMemoryStore 启用高性能内存存储模式");
        }
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Long id = toMemoryId(memoryId);
        if (chatMemoryRepository != null) {
            try {
                return chatMemoryRepository.findById(id)
                        .map(record -> ChatMessageDeserializer.messagesFromJson(record.getMessages()))
                        .orElse(Collections.emptyList());
            } catch (Exception e) {
                log.warn("从数据库读取对话历史异常，回退至内存历史: {}", e.getMessage());
            }
        }
        return inMemoryStorage.getOrDefault(id, Collections.emptyList());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        Long id = toMemoryId(memoryId);
        inMemoryStorage.put(id, messages);
        if (chatMemoryRepository != null) {
            try {
                chatMemoryRepository.upsert(
                        id,
                        ChatMessageSerializer.messagesToJson(messages)
                );
            } catch (Exception e) {
                log.warn("保存对话历史至数据库异常: {}", e.getMessage());
            }
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        Long id = toMemoryId(memoryId);
        inMemoryStorage.remove(id);
        if (chatMemoryRepository != null) {
            try {
                chatMemoryRepository.softDeleteById(id);
            } catch (Exception e) {
                log.warn("从数据库删除对话历史异常: {}", e.getMessage());
            }
        }
    }

    private Long toMemoryId(Object memoryId) {
        if (memoryId == null) {
            return 1L;
        }
        if (memoryId instanceof Number) {
            return ((Number) memoryId).longValue();
        }
        String str = memoryId.toString();
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            long hash = (long) str.hashCode();
            return hash == Long.MIN_VALUE ? 1L : Math.abs(hash);
        }
    }
}
