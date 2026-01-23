package com.hudi.qqboot.listenter;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hudi.qqboot.config.BotConfig;
import com.hudi.qqboot.controller.IdiomDataController;
import com.hudi.qqboot.entity.IdiomData;
import com.hudi.qqboot.service.DeepSeekService;
import com.hudi.qqboot.service.IdiomDataService;
import com.hudi.qqboot.utils.CommonUtils;
import com.hudi.qqboot.vo.UserMessageVo;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.SingleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MessageListener extends SimpleListenerHost {
    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);
    private final DeepSeekService deepSeekService;
    private final static Map<String, List<UserMessageVo>> userData = new HashMap<>();

    @Autowired
    private IdiomDataService idiomDataService;

    private final BotConfig config;

    @Autowired
    public MessageListener(DeepSeekService deepSeekService, BotConfig config) {
        this.deepSeekService = deepSeekService;
        this.config = config;
    }


    @EventHandler
    public void handleGroupMessage(GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        long groupId = event.getGroup().getId();
        String senderName = event.getSenderName();
        String listenerGroup = config.getListenerGroup();
        String qqName = config.getQqName();
        String qq = config.getQq();
        if (StrUtil.isNotBlank(listenerGroup) && listenerGroup.contains(String.valueOf(groupId))) {
            if (message.contains("开启新会话") || message.contains("结束会话")) {
                event.getGroup().sendMessage(new MessageChainBuilder().append("@").append(senderName).append(" 好的，已经开启新会话，让我们重新开始吧！").build());
                userData.remove(senderName);
                return;
            }
            String resMessage = "";
            try {
                if (message.startsWith("@" + qq) || message.startsWith("@" + qqName) || message.startsWith("提问:") || message.startsWith("提问：") || message.startsWith("提问 ")) {
                    message = message.replace("@" + qq, "")
                            .replace("@" + qqName, "")
                            .replace("提问:", "")
                            .replace("提问：", "")
                            .replace("提问 ", "")
                            .trim();
                    String query = "";
                    try {
                        List<UserMessageVo> userMessageVos = userData.computeIfAbsent(senderName, k -> new ArrayList<>());
                        // 先将消息临时拼接，只有消息接口无误，才真的放入消息合计
                        List<UserMessageVo> userMessage = new ArrayList<>(userMessageVos); //临时消息，不放入消息集合，是为了防止接错报错后，该条消息会被计入消息集合中
                        userMessage.add(new UserMessageVo("user", message));
                        query = deepSeekService.queryTemp(userMessage);
                        Map<String, String> resMap = parseRes(query);
                        if (!qq.equals(senderName) && !qqName.equals(senderName)) {
                            if ("true".equals(resMap.get("flag"))) {
                                String reasoning = resMap.get("reasoning");
                                if (StrUtil.isNotBlank(reasoning)) {
                                    resMessage = "思考开始...\n" + reasoning + "\n思考结束。\n\n";
                                }
                                resMessage = resMessage + "@" + senderName + "  " + resMap.get("content");
                                // 只有消息接口无误，才真的放入消息合计
                                userMessageVos.add(new UserMessageVo("user", message));
                                userData.get(senderName).add(new UserMessageVo("assistant", resMap.get("content")));
                            }
                        }
                    } catch (Exception e) {
                        logger.error("异常了: ", e);
                        resMessage = "api接口处理异常";
                    }
                } else if (message.startsWith("成语 ")) {
                    // 随机
                    List<IdiomData> idiomData = idiomDataService.searchByPattern(message);
                    if (!idiomData.isEmpty()) {
                        resMessage = "@" + senderName + " 查询结果：\n" + CommonUtils.getString(message, idiomData);
                    }
                }

            } finally {
                if (StrUtil.isNotBlank(resMessage)) {
                    event.getGroup().sendMessage(new MessageChainBuilder().append(resMessage).build());
                }
            }


        }

    }

    private void handleImageMessage(GroupMessageEvent event) {
        // 遍历消息链获取图片
        for (SingleMessage singleMessage : event.getMessage()) {
            if (singleMessage instanceof Image) {
                Image image = (Image) singleMessage;

                try {

                    // 方案2: 通过getImageId获取图片ID，然后构造字节流
                    String imageId = image.getImageId();
                    byte[] md5 = image.getMd5();


                } catch (Exception e) {
                    logger.error("处理图片失败: ", e);
                }
            }
        }
    }


    public static void main(String[] args) {
        Map<String, List<UserMessageVo>> userData = new HashMap<>();
        userData.computeIfAbsent("11", k -> new ArrayList<>()).add(new UserMessageVo("user", "11"));
        userData.computeIfAbsent("22", k -> new ArrayList<>()).add(new UserMessageVo("user", "21"));
        userData.get("11").add(new UserMessageVo("user", "12"));
        System.out.println("userData = " + userData);
    }

    private Map<String, String> parseRes(String responseBodyString) {
        Map<String, String> content = new HashMap<>();
        content.put("flag", "false");
        try {
            JSONObject jsonObject = JSON.parseObject(responseBodyString);
            JSONArray choices = jsonObject.getJSONArray("choices");
            JSONObject resMessage = choices.getJSONObject(0).getJSONObject("message");
            content.put("content", resMessage.getString("content"));
            content.put("reasoning", resMessage.getString("reasoning_content"));
            content.put("flag", "true");
        } catch (Exception e) {
            content.put("content", "信息处理失败");
        }
        return content;
    }

   /* @EventHandler
    public void handleGroupMessage1(GroupMessageEvent event) {
        String message = event.getMessage().contentToString();
        long groupId = event.getGroup().getId();
        logger.info("Received message: {}", message);
        if ("619955649".equals(String.valueOf(groupId))) {
            deepSeekService.querySync(message).thenAccept(response -> {
                logger.info("DeepSeek response: {}", response);
                event.getGroup().sendMessage(new MessageChainBuilder().append(response).build());
            }).exceptionally(ex -> {
                logger.error("Error handling message", ex);
                event.getGroup().sendMessage(new MessageChainBuilder().append("Error occurred while processing your request.").build());
                return null;
            });
        }
    }*/
}