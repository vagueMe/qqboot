package com.hudi.qqboot.config;

import com.hudi.qqboot.service.ToolsService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.*;
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
        @SystemMessage("您是游戏账号销售。请以友好、乐于助人且愉快的方式来回复。\n" +
                "                        您正在通过在线聊天系统与客户互动。 \n" +
                "                        在提供有关购买或出售的信息之前，您必须始终从用户处获取以下信息：角色名称和账号。\n" +
                "                        请讲中文。\n" +
                "\t\t\t\t\t   今天的日期是 {{currentDate}}.")
        TokenStream stream(@MemoryId String memoryId, @UserMessage String message, @V("currentDate") String currentDate);
    }

    @Bean
    public AssistantUnique assistantUnique(QwenChatModel qwenChatModel,
                                           QwenStreamingChatModel qwenStreamingChatModel, ToolsService toolsService) {
        PersistentChatMemoryStore store = new PersistentChatMemoryStore();

        ChatMemoryProvider provider = memoryId ->  MessageWindowChatMemory.builder()
                    .id(memoryId)
                    .maxMessages(10)
                    .chatMemoryStore(store)
                    .build();

        AssistantUnique assistant = AiServices.builder(AssistantUnique.class)
                .chatModel(qwenChatModel)
                .tools(toolsService)
                .streamingChatModel(qwenStreamingChatModel)
//                .chatMemoryProvider(memoryId ->
//                        MessageWindowChatMemory.builder().maxMessages(10).id(memoryId).build()
//                ).build();
                .chatMemoryProvider(provider).build();


        return assistant;
    }
}
