package com.hudi.qqboot.service;

import com.hudi.qqboot.config.NapCatQQConfig;
import com.hudi.qqboot.vo.UserMessageVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QQ消息处理服务
 * 提供与NapCatQQ交互的相关功能
 */
@Service
public class QQMessageService {

    private static final Logger logger = LoggerFactory.getLogger(QQMessageService.class);

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private NapCatQQConfig napCatQQConfig;
    /**
     * 发送消息到指定用户或群
     *
     * @param targetId 目标ID（用户ID或群ID）
     * @param message  消息内容
     * @param isGroup  是否为群消息
     * @return 发送结果
     */
    public Map<String, Object> sendMessage(String targetId, String message, boolean isGroup) {
        try {
            // 构建发送消息的参数
            Map<String, Object> params = new HashMap<>();
            if (isGroup) {
                params.put("group_id", targetId);
            } else {
                params.put("user_id", targetId);
            }
            params.put("message", message);

            // 这里需要根据实际的NapCatQQ API端点进行配置
            // 发送HTTP请求到NapCatQQ
            RestTemplate restTemplate = new RestTemplate();
            Map response = restTemplate.postForObject(napCatQQConfig.getUrl() + "/send_msg", params, Map.class);

            logger.info("发送消息到NapCatQQ: 目标={}, 消息={}, 结果={}", targetId, message, response);
            return response != null ? response : Map.of("status", "success");
        } catch (Exception e) {
            logger.error("发送消息到NapCatQQ失败", e);
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    /**
     * 使用AI服务处理消息并返回结果
     *
     * @param message 用户消息
     * @param userId  用户ID
     * @return AI处理后的回复
     */
    public String processMessageWithAI(String message, String userId) {
        try {
            // 创建消息对象
            UserMessageVo userMsg = new UserMessageVo("user", message);

            // 创建消息列表
            List<UserMessageVo> messages = List.of(userMsg);

            // 调用AI服务
            String aiResponse = deepSeekService.queryTemp(messages);

            // 解析AI响应
            return parseAIResponse(aiResponse);
        } catch (Exception e) {
            logger.error("处理AI消息时发生错误", e);
            return "很抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    /**
     * 解析AI响应
     *
     * @param aiResponse AI响应内容
     * @return 解析后的消息内容
     */
    private String parseAIResponse(String aiResponse) {
        try {
            // 尝试解析JSON响应
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(aiResponse, Map.class);

            Object choicesObj = responseMap.get("choices");
            if (choicesObj instanceof List) {
                List<?> choicesList = (List<?>) choicesObj;
                if (!choicesList.isEmpty()) {
                    Object firstChoice = choicesList.get(0);
                    if (firstChoice instanceof Map) {
                        Map<?, ?> choiceMap = (Map<?, ?>) firstChoice;
                        Object messageObj = choiceMap.get("message");
                        if (messageObj instanceof Map) {
                            Map<?, ?> messageMap = (Map<?, ?>) messageObj;
                            Object content = messageMap.get("content");
                            if (content instanceof String) {
                                return (String) content;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("解析AI响应失败，返回原始响应", e);
        }

        // 如果解析失败，返回原始响应的简化版本
        if (aiResponse.length() > 500) {
            return aiResponse.substring(0, 500) + "...";
        }
        return aiResponse;
    }

    /**
     * 处理特殊命令
     *
     * @param command 命令内容
     * @return 命令执行结果
     */
    public String handleCommand(String command) {
        if (command == null) {
            return null;
        }

        switch (command.toLowerCase()) {
            case "/help":
                return "欢迎使用QQ机器人！\n" +
                       "- 发送普通消息与AI聊天\n" +
                       "- 使用 /help 查看帮助\n" +
                       "- 使用 /about 查看关于信息\n" +
                       "- 使用 /ping 检查机器人状态";
            case "/about":
                return "这是一个基于NapCatQQ和Spring Boot的智能QQ机器人。\n" +
                       "使用DeepSeek AI模型提供智能对话功能。";
            case "/ping":
                return "机器人在线，状态正常！";
            default:
                return null; // 不是特殊命令，返回null
        }
    }
}