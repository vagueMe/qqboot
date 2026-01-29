package com.hudi.qqboot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bot.napcatqq")
@Data
public class NapCatQQConfig {

    /**
     * 是否启用NapCatQQ接口
     */
    private boolean enabled = true;

    /**
     * NapCatQQ消息接收端点
     */
    private String endpoint = "/napcatqq/message";

    /**
     * 消息处理超时时间（毫秒）
     */
    private long timeout = 5000;

    /**
     * 访问令牌，用于验证请求来源
     */
    private String accessToken;

    /**
     * 访问地址
     */
    private String url;


}