package com.hudi.qqboot.service;

import com.hudi.qqboot.config.BotConfig;
import com.hudi.qqboot.vo.UserMessageVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DeepSeekService {
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekService.class);
    //    private final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS).build();
    private final BotConfig config;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10); // 调整线程池大小
    private Map<String, List<String>> cache = new java.util.HashMap<>();
    @Autowired
    public DeepSeekService(BotConfig config) {
        this.config = config;
    }

    public String queryTemp(List<UserMessageVo>  prompt) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(200001); // 设置连接超时时间(单位:毫秒)
        requestFactory.setReadTimeout(200001); // 设置读取超时时间(单位:毫秒)

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        // deepseek-chat deepseek-reasoner
//        String data = "{\"messages\":[{\"content\":\"" + prompt + "\",\"role\":\"user\"}],\"model\":\"deepseek-reasoner\",\"frequency_penalty\":0,\"max_tokens\":2048,\"presence_penalty\":0,\"response_format\":{\"type\":\"text\"},\"stop\":null,\"stream\":false,\"stream_options\":null,\"temperature\":1,\"top_p\":1,\"tools\":null,\"tool_choice\":\"none\",\"logprobs\":false,\"top_logprobs\":null}";
        String data = "{\n  \"messages\": " + toJson(prompt) +",\n  \"model\": \"deepseek-chat\",\n  \"thinking\": {\n    \"type\": \"enabled\"\n  },\n  \"frequency_penalty\": 0,\n  \"max_tokens\": 4096,\n  \"presence_penalty\": 0,\n  \"response_format\": {\n    \"type\": \"text\"\n  },\n  \"stop\": null,\n  \"stream\": false,\n  \"stream_options\": null,\n  \"temperature\": 1,\n  \"top_p\": 1,\n  \"tools\": null,\n  \"tool_choice\": \"none\",\n  \"logprobs\": false,\n  \"top_logprobs\": null\n}";
        System.out.println("请求data = " + data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/json; charset=UTF-8"));
        headers.set("Accept", org.springframework.http.MediaType.APPLICATION_JSON.toString());
        headers.set("Authorization", "Bearer " + config.getDeepSeekApiKey());

        HttpEntity<String> requestEntity = new HttpEntity<>(data, headers);
        String s = restTemplate.postForObject(config.getDeepSeekApiUrl(), requestEntity, String.class);
        System.out.println("响应data = " + s);
        return s;
    }

    // 简单的JSON转换方法
    private String toJson(List<UserMessageVo> messages) {
        StringBuilder sb = new StringBuilder("[");
        UserMessageVo message;
        for (int i = 0; i < messages.size(); i++) {
            message = messages.get(i);
            sb.append("{")
                    .append("\"role\": \"").append(escapeJson(message.getRole())).append("\",")
                    .append("\"content\": \"").append(escapeJson(message.getContent())).append("\"")
                    .append("}");
            if (i < messages.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escapeJson(String content) {
        if (content == null) return "";
        return content.replace("\\", "")
                .replace("\"", "\\\"")
                .replace("\n", "")
                .replace("\r", "")
                .replace("\t", "");
    }

    /* public String query(String prompt) {
         String data = "{\"messages\":[{\"content\":\"" + prompt + "\",\"role\":\"user\"}],\"model\":\"deepseek-reasoner\",\"frequency_penalty\":0,\"max_tokens\":2048,\"presence_penalty\":0,\"response_format\":{\"type\":\"text\"},\"stop\":null,\"stream\":false,\"stream_options\":null,\"temperature\":1,\"top_p\":1,\"tools\":null,\"tool_choice\":\"none\",\"logprobs\":false,\"top_logprobs\":null}";
         System.out.println("请求data = " + data);
         RequestBody body = RequestBody.create(
                 data,
                 MediaType.parse("application/json; charset=utf-8")
         );

         Request request = new Request.Builder()
                 .url(config.getDeepSeekApiUrl())
                 .header("Authorization", "Bearer " + config.getDeepSeekApiKey())
                 .post(body)
                 .build();

         try (Response response = client.newCall(request).execute()) {
             if (response.isSuccessful() && response.body() != null) {
 //                    responseBodyString = getString(responseBodyString);
                 return response.body().string();
             } else {
                 logger.error("Failed to get response from DeepSeek API: {},msg: {}", response.code(), response.toString());
                 return "Failed to get response from DeepSeek API.";
             }
         } catch (IOException e) {
             logger.error("Error occurred while querying DeepSeek API", e);
             return "Error occurred while querying DeepSeek API.";
         }
     }

     public CompletableFuture<String> querySync(String prompt) {
         return CompletableFuture.supplyAsync(() -> {
             String data = "{\"messages\":[{\"content\":\"" + prompt + "\",\"role\":\"user\"}],\"model\":\"deepseek-reasoner\",\"frequency_penalty\":0,\"max_tokens\":2048,\"presence_penalty\":0,\"response_format\":{\"type\":\"text\"},\"stop\":null,\"stream\":false,\"stream_options\":null,\"temperature\":1,\"top_p\":1,\"tools\":null,\"tool_choice\":\"none\",\"logprobs\":false,\"top_logprobs\":null}";
             System.out.println("请求data = " + data);
             RequestBody body = RequestBody.create(
                     data,
                     MediaType.parse("application/json; charset=utf-8")
             );

             Request request = new Request.Builder()
                     .url(config.getDeepSeekApiUrl())
                     .header("Authorization", "Bearer " + config.getDeepSeekApiKey())
                     .post(body)
                     .build();

             try (Response response = client.newCall(request).execute()) {
                 if (response.isSuccessful() && response.body() != null) {
                     String string = response.body().toString();
                     logger.info("api query success: {}", string);
 //                    responseBodyString = getString(responseBodyString);
                     return string;
                 } else {
                     logger.error("Failed to get response from DeepSeek API: {},msg: {}", response.code(), response.toString());
                     return "Failed to get response from DeepSeek API.";
                 }
             } catch (IOException e) {
                 logger.error("Error occurred while querying DeepSeek API", e);
                 return "Error occurred while querying DeepSeek API.";
             }
         }, executorService);
     }
 */
}