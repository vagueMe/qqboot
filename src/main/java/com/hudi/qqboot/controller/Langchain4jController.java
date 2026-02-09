package com.hudi.qqboot.controller;

import com.hudi.qqboot.config.BotConfig;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.*;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

/**
 * DeepSeek 流式响应控制器
 * 提供真正的服务器推送事件(SSE)流式接口
 *
 * @author hudi
 * @date 30 1月 2026 15:50
 */
@Slf4j
@RestController
@RequestMapping("/langchain")
public class Langchain4jController {

    @Autowired
    private BotConfig botConfig;

    /**
     * 基于 LangChain4j 和 DeepSeek 的流式响应接口
     * 使用真正的 AI 流式对话能力，支持思考过程和响应内容的分离传输
     *
     * @param prompt 用户输入的提示词
     * @return Flux<String> 流式响应数据，格式为 JSON
     */
    @GetMapping(value = "/langchain-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> langchainStream(@RequestParam String prompt) {
        log.info("收到 LangChain4j 流式请求: {}", prompt);

//        StreamingChatModel model = dsmodel();
        StreamingChatModel model = qwmodel();


        // 创建 Sin ks 用于流式响应
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        model.chat(prompt, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
//                log.info("接收到部分响应: {}", partialResponse);
                sink.tryEmitNext(partialResponse);
            }

            @Override
            public void onPartialThinking(PartialThinking partialThinking) {
                log.debug("接收到思考过程: {}", partialThinking);
            }

            @Override
            public void onPartialToolCall(PartialToolCall partialToolCall) {
                log.debug("接收到工具调用: {}", partialToolCall);
            }

            @Override
            public void onCompleteToolCall(CompleteToolCall completeToolCall) {
                log.debug("完成工具调用: {}", completeToolCall);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                log.info("完成完整响应");
                sink.tryEmitComplete(); // 结束流
            }

            @Override
            public void onError(Throwable error) {
                log.error("流式响应出错: ", error);
                String errorMessage = error.getMessage() != null ? error.getMessage() : "未知错误";
                sink.tryEmitNext(formatSseMessage("error", errorMessage, "处理出错"));
                sink.tryEmitError(error); // 发送错误信息
            }
        });
        return sink.asFlux();
    }

    private StreamingChatModel dsmodel() {
        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(botConfig.getDeepSeekApiKey())
                .baseUrl("https://api.deepseek.com")
//                .baseUrl(botConfig.getDee pSeekApiUrl())
                .modelName("deepseek-chat")
                .temperature(0.7)
                .maxTokens(1000)
                .build();
        return model;
    }

    private StreamingChatModel qwmodel() {
        StreamingChatModel model = QwenStreamingChatModel.builder()
                .apiKey(botConfig.getQwApiKey())
                .baseUrl("https://dashscope.aliyuncs.com/api/v1")
                .modelName("qwen3-max")
                .maxTokens(1000)
                .build();
        return model;
    }

    public static void main(String[] args) {

    }

    /**
     * 流式文本生成接口 - 真正的流式响应
     * 使用 Server-Sent Events (SSE) 实现逐字推送
     *
     * @param prompt 用户输入的提示词
     * @return Flux<String> 流式响应数据
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamText(@RequestParam String prompt) {
        log.info("收到流式请求: {}", prompt);

        // 模拟AI逐步生成响应的过程
        String[] words = {
                "正在思考您的问题...\n",
                "分析中...\n",
                "基于您的输入 \"" + prompt + "\"，\n",
                "我可以为您提供以下见解：\n\n",
                "• 这是一个很有意思的问题\n",
                "• 需要考虑多个方面\n",
                "• 让我逐步为您分析\n\n",
                "具体来说：\n",
                "1. 首先要理解核心概念\n",
                "2. 然后分析关键要素\n",
                "3. 最后给出实用建议\n\n",
                "希望这个回答对您有帮助！\n",
                "如有其他问题，欢迎继续提问。"
        };

        // 使用 Sinks 创建可编程的 Flux
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        // 异步发送数据
        Flux.interval(Duration.ofMillis(300)) // 每300毫秒发送一次
                .take(words.length) // 发送指定数量的数据
                .doOnNext(index -> {
                    int idx = index.intValue();
                    String word = words[idx];
                    log.debug("发送第{}个片段: {}", idx + 1, word.trim());
                    sink.tryEmitNext("data: " + word + "\n\n");
                })
                .doOnComplete(() -> {
                    log.info("流式响应完成");
                    sink.tryEmitComplete();
                })
                .doOnError(error -> {
                    log.error("流式响应出错: {}", error.getMessage());
                    sink.tryEmitError(error);
                })
                .subscribe();

        return sink.asFlux();
    }

    /**
     * 实时数据推送接口
     * 模拟实时数据流（如股票价格、系统监控等）
     *
     * @param dataType 数据类型
     * @return Flux<String> 实时数据流
     */
    @GetMapping(value = "/realtime", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> realtimeData(@RequestParam(defaultValue = "system") String dataType) {
        log.info("启动实时数据推送: {}", dataType);

        return Flux.interval(Duration.ofSeconds(1))
                .map(index -> {
                    String data;
                    switch (dataType.toLowerCase()) {
                        case "stock":
                            data = String.format("{\"symbol\":\"AAPL\",\"price\":%.2f,\"timestamp\":%d}",
                                    150.0 + Math.random() * 20, System.currentTimeMillis());
                            break;
                        case "system":
                            data = String.format("{\"cpu\":%.1f,\"memory\":%.1f,\"timestamp\":%d}",
                                    Math.random() * 100, Math.random() * 100, System.currentTimeMillis());
                            break;
                        default:
                            data = String.format("{\"message\":\"Hello %d\",\"timestamp\":%d}",
                                    index, System.currentTimeMillis());
                    }
                    return "data: " + data + "\n\n";
                })
                .doOnCancel(() -> log.info("客户端断开实时数据连接"));
    }

    /**
     * 流式文件内容传输
     * 模拟大文件分块传输
     *
     * @param fileName 文件名
     * @return Flux<String> 文件内容流
     */
    @GetMapping(value = "/file-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamFile(@RequestParam String fileName) {
        log.info("开始流式传输文件: {}", fileName);

        // 模拟文件内容
        String[] fileChunks = {
                "文件头部信息...\n",
                "===================\n",
                "文件内容第一部分\n",
                "这是文件的核心内容\n",
                "包含重要数据和信息\n",
                "===================\n",
                "文件尾部信息...\n",
                "传输完成 [EOF]\n"
        };

        return Flux.fromArray(fileChunks)
                .delayElements(Duration.ofMillis(500))
                .map(chunk -> "data: " + chunk + "\n\n")
                .doOnComplete(() -> log.info("文件流式传输完成"));
    }

    /**
     * 格式化 SSE 消息
     *
     * @param type        消息类型 (thinking/content/tool_call/tool_result/status/error)
     * @param content     消息内容
     * @param description 描述信息
     * @return 格式化的 SSE 消息
     */
    private String formatSseMessage(String type, String content, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append("data: {");
        sb.append("\"type\": \"").append(type).append("\",");
        if (content != null) {
            sb.append("\"content\": \"").append(escapeJson(content)).append("\",");
        }
        if (description != null) {
            sb.append("\"description\": \"").append(escapeJson(description)).append("\",");
        }
        sb.append("\"timestamp\": ").append(System.currentTimeMillis());
        sb.append("}\n\n");
        return sb.toString();
    }

    /**
     * 转义 JSON 字符串
     *
     * @param str 待转义的字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 提取思考内容
     *
     * @param partialThinking PartialThinking 对象
     * @return 提取的思考内容字符串
     */
    private String extractThinkingContent(PartialThinking partialThinking) {
        try {
            // 尝试获取思考内容的不同方式
            String content = partialThinking.toString();
            if (content != null && !content.isEmpty()) {
                // 如果是复杂的对象，尝试提取有用信息
                if (content.contains("reasoning") || content.contains("thought")) {
                    return content;
                }
                // 返回简化的内容
                return content.length() > 100 ? content.substring(0, 100) + "..." : content;
            }
        } catch (Exception e) {
            log.warn("提取思考内容时出错: ", e);
        }
        return "正在思考...";
    }

    /**
     * 访问优化版流式演示页面
     */
    @GetMapping("/demo")
    public String optimizedDemo() {
        return "optimized-stream-demo";
    }
}