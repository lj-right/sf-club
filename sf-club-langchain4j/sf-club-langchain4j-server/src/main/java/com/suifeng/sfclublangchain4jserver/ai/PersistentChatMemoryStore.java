package com.suifeng.sfclublangchain4jserver.ai;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.List;

public class PersistentChatMemoryStore implements ChatMemoryStore {

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // TODO: 实现通过 memory ID 从持久化存储中获取所有消息。
        // 可使用 ChatMessageDeserializer.messageFromJson(String)
        // 和 ChatMessageDeserializer.messagesFromJson(String) 辅助方法从 JSON 反序列化。
        return null;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // TODO: 实现通过 memory ID 更新持久化存储中的所有消息。
        // 可使用 ChatMessageSerializer.messageToJson(ChatMessage)
        // 和 ChatMessageSerializer.messagesToJson(List<ChatMessage>) 辅助方法将消息序列化为 JSON。
    }

    @Override
    public void deleteMessages(Object memoryId) {
        // TODO: 实现通过 memory ID 删除持久化存储中的所有消息。
    }
}
//
//ChatMemory chatMemory = MessageWindowChatMemory.builder()
//        .id("12345")
//        .maxMessages(10)
//        .chatMemoryStore(new PersistentChatMemoryStore())
//        .build();
