package com.hudi.qqboot.controller;

import com.hudi.qqboot.config.AiConfig;
import com.hudi.qqboot.config.PersistentChatMemoryStore;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author hudi
 * @date 05 2月 2026 15:32
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {

    private static ChatMemory chatMemory;

    static {
        chatMemory = MessageWindowChatMemory.builder()
                .id("12345")
                .maxMessages(10)
                .chatMemoryStore(new PersistentChatMemoryStore())
                .build();
    }

    @Autowired
    QwenChatModel chatModel;

    @Autowired
    QwenStreamingChatModel streamingChatModel;

    @Autowired
    AiConfig.Assistant assistant;

    @RequestMapping("/chat")
    public String chat(@RequestParam(defaultValue = "你是谁") String prompt) {
//        String chat = chatModel.chat("我是hudi");
//        ChatMessage use = new UserMessage("我是hudi");
//        ChatResponse chat1 = chatModel.chat(use);
//        System.out.println(chat1.aiMessage().text());
//        ChatMessage use2 = new UserMessage("我是谁？");
//        ChatResponse chat2 = chatModel.chat(use, chat1.aiMessage(), use2);
//        System.out.println(chat2.aiMessage().text());
//
//        return chat2.aiMessage().text();
//        chatMemory.add(new UserMessage(prompt));
//        AiMessage aiMessage = chatModel.chat(chatMemory.messages()).aiMessage();
//        chatMemory.add(aiMessage);
//        return aiMessage.text();

//        String chat = assistant.chat(prompt);

        return null;
    }

    @RequestMapping(value = "/streamChat", produces = "text/stream;charset=UTF-8")
    public Flux<String> streamChat(@RequestParam(defaultValue = "你是谁") String prompt) {

        Flux<String> objectFlux = Flux.create(sink -> {
            streamingChatModel.chat(prompt, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String s) {
                    sink.next(s);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    sink.complete();  // 完成整个响应流
                }

                @Override
                public void onError(Throwable error) {
                    sink.error(error);  // 异常处理
                }
            });
        });

        /*
        // 会报错 - 不可行
        Flux<String> objectFlux = Flux.create(sink -> {
            assistant.chat(prompt, new StreamingChatResponseHandler() {

                @Override
                public void onPartialResponse(String s) {
                    sink.next(s);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    sink.complete();  // 完成整个响应流
                }

                @Override
                public void onError(Throwable error) {
                    sink.error(error);  // 异常处理
                }
            });
        });*/

        return objectFlux;
    }
}
