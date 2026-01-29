package com.hudi.qqboot;

import cn.hutool.core.util.StrUtil;
import com.hudi.qqboot.config.BotConfig;
import com.hudi.qqboot.listenter.MessageListener;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.auth.BotAuthorization;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.utils.BotConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

//@SpringBootApplication
public class QqbootListenerApplication implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(QqbootListenerApplication.class);


    //        @Autowired
    private BotConfig botConfig;

    //        @Autowired
    private MessageListener messageListener;

    public static void main(String[] args) {
        SpringApplication.run(QqbootListenerApplication.class, args);
        logger.info("----------------------------------------------------------" +
                "ListenerApplication is running! " +
                "----------------------------------------------------------");
    }

    @Override
    public void run(String... args) {
        String qq = botConfig.getQq();
        if (StrUtil.isBlank(qq)) {
            logger.error("QQ号为空，程序终止");
            return;
        }
        BotConfiguration configuration = BotConfiguration.getDefault();
        configuration.setProtocol(BotConfiguration.MiraiProtocol.MACOS);
        configuration.fileBasedDeviceInfo("device.json");
        // 使用二维码认证
        Bot bot = BotFactory.INSTANCE.newBot(Long.parseLong(botConfig.getQq()), BotAuthorization.byQRCode(), configuration);
        bot.login();
        GlobalEventChannel.INSTANCE.registerListenerHost(messageListener);
        logger.info("QQ Bot started successfully.");

    }
}