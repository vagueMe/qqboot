package com.hudi.qqboot.config;

import com.hudi.qqboot.service.ToolsService;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @author hudi
 * @date 05 2月 2026 18:12
 */
@Configuration
public class AiConfig {

    @Value("${baiduMapApiKey}")
    private String baiduMapApiKey;

    public static int tempInt = 0;

    @Bean
    public EmbeddingStore getEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

//    private static InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
//
//    // 使用单利模式获取embeddingStore
//    public static EmbeddingStore<TextSegment> getEmbeddingStore() {
//        if (embeddingStore == null) {
//           synchronized (EmbeddingStore.class) {
//               if (embeddingStore == null) {
//                   embeddingStore = new InMemoryEmbeddingStore<>();
//               }
//           }
//        }
//        return embeddingStore;
//    }


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

        return assistant;
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
                                           QwenStreamingChatModel qwenStreamingChatModel,
                                           ToolsService toolsService,
                                           QwenEmbeddingModel qwenEmbeddingModel,
                                           EmbeddingStore<TextSegment> embeddingStore
    ) {
        PersistentChatMemoryStore store = new PersistentChatMemoryStore();

        // 对话记忆
        ChatMemoryProvider provider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(store)
                .build();

        // 内容检索器
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(4)
                .minScore(0.7)
                .build();

        AssistantUnique assistant = AiServices.builder(AssistantUnique.class)
                .chatModel(qwenChatModel)
                .tools(toolsService)
                .toolProvider(returnToolProvider())
                .streamingChatModel(qwenStreamingChatModel)
                .contentRetriever(contentRetriever)
//                .chatMemoryProvider(memoryId ->
//                        MessageWindowChatMemory.builder().maxMessages(10).id(memoryId).build()
//                ).build();
                .chatMemoryProvider(provider).build();


        return assistant;
    }

    public McpToolProvider returnToolProvider() {

        // 2.构建MCP服务传输方式  有sse和stdio两种， 这里演示的是stdio
        StdioMcpTransport transport = new StdioMcpTransport.Builder().command(
                List.of( "cmd", "/c", "npx", "-y", "@baidumap/mcp-server-baidu-map")
        ).environment(Map.of("BAIDU_MAP_API_KEY", baiduMapApiKey))
        .logEvents(true)
        .build();

        // 构建mcp客户端，指定传输方式
        DefaultMcpClient mcpClient = new DefaultMcpClient.Builder().transport(transport).build();
        // 构建mcp工具提工者，指定mcp客户端
        return McpToolProvider.builder().mcpClients(List.of(mcpClient)).build();
    }
}
