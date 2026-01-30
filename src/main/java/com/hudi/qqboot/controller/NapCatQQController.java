package com.hudi.qqboot.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.hudi.qqboot.config.BotConfig;
import com.hudi.qqboot.config.NapCatQQConfig;
import com.hudi.qqboot.service.DeepSeekService;
import com.hudi.qqboot.service.QQMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * NapCatQQ消息接收控制器
 * 用于接收来自NapCatQQ的HTTP消息推送
 */
@RestController
@RequestMapping("/napcatqq")
@ConditionalOnProperty(name = "bot.napcatqq.enabled", havingValue = "true", matchIfMissing = true)
public class NapCatQQController {

    private static final Logger logger = LoggerFactory.getLogger(NapCatQQController.class);

    @Autowired
    private BotConfig botConfig;

    @Autowired
    private NapCatQQConfig napCatQQConfig;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private QQMessageService qqMessageService;

    /**
     * 接收NapCatQQ的消息推送
     * 消息格式通常是JSON格式，包含消息ID、发送者、消息内容等信息
     *
     * @param messageData 消息数据
     * @return 处理结果
     */
    @PostMapping("/message")
    public Map<String, Object> receiveMessage(@RequestBody Map<String, Object> messageData) {
        logger.info("接收到NapCatQQ消息: {}", messageData);

        try {
            // 验证访问令牌（如果有配置）
            String accessToken = (String) messageData.get("access_token");
            if (StrUtil.isNotBlank(napCatQQConfig.getAccessToken()) &&
                    !napCatQQConfig.getAccessToken().equals(accessToken)) {
                logger.warn("无效的访问令牌: {}", accessToken);
                return Map.of("retcode", 401, "status", "failed", "message", "Unauthorized");
            }

            // 解析消息数据
            String messageType = messageData.getOrDefault("message_type", "").toString();
            String userId = messageData.getOrDefault("user_id", "").toString();
            String groupId = messageData.getOrDefault("group_id", "").toString();
            String rawMessage = messageData.getOrDefault("raw_message", "").toString();
            String messageId = messageData.getOrDefault("message_id", "").toString();
            String message = messageData.getOrDefault("message", "").toString();

            // 如果raw_message为空，尝试从message字段获取
            if (rawMessage == null || rawMessage.isEmpty()) {
                rawMessage = message;
            }

            logger.info("消息类型: {}, 用户ID: {}, 群ID: {}, 原始消息: {}, 消息ID: {}",
                    messageType, userId, groupId, rawMessage, messageId);

            // 根据消息类型进行不同处理
            List<String> msgBeginList = botConfig.getMsgBegin();
            List<String> listenerGroup = botConfig.getListenerGroup();
            if (CollectionUtil.isNotEmpty(msgBeginList)) {
                for (String i : msgBeginList) {
                    if (message.startsWith(i)) {
                        if ("message".equals(messageType) || ("group".equals(messageType) && listenerGroup.contains(groupId)) || "private".equals(messageType)) {
                            // 异步处理消息以避免超时
                            processUserMessageAsync(userId, groupId, rawMessage, messageData);
                        }
                    }
                }
            }
//            }
            // 返回成功响应，告诉NapCatQQ消息已接收
            return Map.of("retcode", 0, "status", "ok");
        } catch (Exception e) {
            logger.error("处理NapCatQQ消息时发生错误", e);
            return Map.of("retcode", 1, "status", "error", "message", e.getMessage());
        }
    }

    /**
     * 心跳检测接口
     * 用于测试NapCatQQ与后端服务的连接状态
     */
    @GetMapping("/heartbeat")
    public Map<String, Object> heartbeat() {
        logger.info("NapCatQQ心跳检测");
        return Map.of("status", "online", "message", "NapCatQQ接口正常");
    }

    /**
     * 异步处理用户消息
     */
    private void processUserMessageAsync(String userId, String groupId, String message, Map<String, Object> fullMessageData) {
        // 在新线程中处理消息，避免阻塞HTTP响应
        new Thread(() -> {
            try {
                String response = handleUserMessage(userId, groupId, message);

                // 如果有回复，可以通过API发送回去
                if (response != null && !response.isEmpty()) {
                    sendReplyToNapCatQQ(userId, groupId, response);
                }
            } catch (Exception e) {
                logger.error("异步处理用户消息时发生错误", e);
            }
        }).start();
    }

    /**
     * 处理用户消息的具体逻辑
     */
    private String handleUserMessage(String userId, String groupId, String message) {
        logger.info("处理用户消息 - 用户: {}, 群: {}, 消息: {}", userId, groupId, message);

        try {
            // 检查是否为特殊命令
            String commandResult = qqMessageService.handleCommand(message);
            if (commandResult != null) {
                return commandResult;
            }
            if (StrUtil.isNotBlank(message) && message.startsWith("测试提问")) {
                message = message.replace("测试提问", "");
                return qqMessageService.processMessageWithAI(message, userId);
            }
            // 使用AI服务处理消息
            return "";
        } catch (Exception e) {
            logger.error("处理用户消息时发生错误", e);
            return "处理消息时发生错误，请稍后再试。";
        }
    }


    /**
     * 发送回复到NapCatQQ
     */
    private void sendReplyToNapCatQQ(String userId, String groupId, String response) {
        // 使用QQMessageService发送回复
        if (StrUtil.isNotBlank(groupId)) {
            // 发送到群
            qqMessageService.sendMessage(groupId, response, true);
        } else {
            // 发送到用户
            qqMessageService.sendMessage(userId, response, false);
        }
    }

    /**
     * 通用事件上报接口
     * 处理NapCatQQ的各种事件上报
     */
    @PostMapping("/event")
    public Map<String, Object> receiveEvent(@RequestBody Map<String, Object> eventData) {
        logger.info("接收到NapCatQQ事件: {}", eventData);

        try {
            String eventType = (String) eventData.get("post_type");
            String detailType = (String) eventData.get("detail_type");

            logger.info("事件类型: {}, 详细类型: {}", eventType, detailType);

            // 根据不同事件类型进行处理
            switch (eventType) {
                case "notice":
                    handleNoticeEvent(detailType, eventData);
                    break;
                case "request":
                    handleRequestEvent(detailType, eventData);
                    break;
                case "meta_event":
                    handleMetaEvent(detailType, eventData);
                    break;
                default:
                    logger.info("未处理的事件类型: {}", eventType);
            }

            return Map.of("retcode", 0, "status", "ok");
        } catch (Exception e) {
            logger.error("处理NapCatQQ事件时发生错误", e);
            return Map.of("retcode", 1, "status", "error", "message", e.getMessage());
        }
    }

    /**
     * 处理通知事件
     */
    private void handleNoticeEvent(String detailType, Map<String, Object> eventData) {
        logger.info("处理通知事件: {}", detailType);

        switch (detailType) {
            case "group_upload":
                logger.info("群文件上传");
                break;
            case "group_admin":
                logger.info("群管理员变动");
                break;
            case "group_decrease":
                logger.info("群成员减少");
                break;
            case "group_increase":
                logger.info("群成员增加");
                break;
            default:
                logger.info("未处理的通知事件: {}", detailType);
        }
    }

    /**
     * 处理请求事件
     */
    private void handleRequestEvent(String detailType, Map<String, Object> eventData) {
        logger.info("处理请求事件: {}", detailType);

        switch (detailType) {
            case "friend":
                logger.info("好友添加请求");
                break;
            case "group":
                logger.info("群聊邀请或加群请求");
                break;
            default:
                logger.info("未处理的请求事件: {}", detailType);
        }
    }

    /**
     * 处理元事件
     */
    private void handleMetaEvent(String detailType, Map<String, Object> eventData) {
        logger.info("处理元事件: {}", detailType);

        switch (detailType) {
            case "lifecycle":
                logger.info("生命周期事件");
                break;
            case "heartbeat":
                logger.info("心跳事件");
                break;
            default:
                logger.info("未处理的元事件: {}", detailType);
        }
    }
}
