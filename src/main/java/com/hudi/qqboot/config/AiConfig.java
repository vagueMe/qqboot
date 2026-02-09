package com.hudi.qqboot.config;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hudi
 * @date 05 2月 2026 18:12
 */
@Configuration
public class AiConfig {
    public interface Assistant {
        // 可行
        String chat(String message);
        // 流式响应
        TokenStream stream(String message);
    }

    @Bean
    public Assistant assistant(QwenChatModel qwenChatModel,
                               QwenStreamingChatModel qwenStreamingChatModel) {
//        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id("12345")
                .maxMessages(10)
                .chatMemoryStore(new PersistentChatMemoryStore()) //  数据库记忆的
                .build();


        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(qwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .build();

        return  assistant;
    }


    // @MemoryId  @UserMessage  必填
    public interface AssistantUnique {
        // 可行
        String chat(@MemoryId String memoryId, @UserMessage String message);
        // 流式响应
        TokenStream stream(@MemoryId String memoryId, @UserMessage String message);
    }

    @Bean
    public AssistantUnique assistantUnique(QwenChatModel qwenChatModel,
                               QwenStreamingChatModel qwenStreamingChatModel) {
        PersistentChatMemoryStore store = new PersistentChatMemoryStore();

        ChatMemoryProvider provider = memoryId ->  MessageWindowChatMemory.builder()
                    .id(memoryId)
                    .maxMessages(10)
                    .chatMemoryStore(store)
                    .build();

        AssistantUnique assistant = AiServices.builder(AssistantUnique.class)
                .chatModel(qwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
//                .chatMemoryProvider(memoryId ->
//                        MessageWindowChatMemory.builder().maxMessages(10).id(memoryId).build()
//                ).build();
                .chatMemoryProvider(provider).build();


        return assistant;
    }
}
