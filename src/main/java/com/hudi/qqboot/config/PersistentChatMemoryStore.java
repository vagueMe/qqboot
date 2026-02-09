package com.hudi.qqboot.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hudi  -- 这个类可以和数据库进行交互，将消息进行持久化
 * @date 05 2月 2026 17:14
 */
public class PersistentChatMemoryStore implements ChatMemoryStore {

    Map<String, List<ChatMessage> > temp = new HashMap<>();
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // TODO: Implement getting all messages from the persistent store by memory ID.
        // ChatMessageDeserializer.messageFromJson(String) and
        // ChatMessageDeserializer.messagesFromJson(String) helper methods can be used to
        // easily deserialize chat messages from JSON.
        return temp.computeIfAbsent((String) memoryId, k -> new ArrayList<>());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        // TODO: Implement updating all messages in the persistent store by memory ID.
        // ChatMessageSerializer.messageToJson(ChatMessage) and
        // ChatMessageSerializer.messagesToJson(List<ChatMessage>) helper methods can be used to
        // easily serialize chat messages into JSON.
        temp.put((String) memoryId, messages);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        // TODO: Implement deleting all messages in the persistent store by memory ID.
        temp.remove(memoryId);
    }
}