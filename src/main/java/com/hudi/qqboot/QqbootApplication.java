package com.hudi.qqboot;

import cn.hutool.core.util.StrUtil;
import com.hudi.qqboot.config.BotConfig;
import com.hudi.qqboot.listenter.MessageListener;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.auth.BotAuthorization;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.LoginSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
public class QqbootApplication extends SpringBootServletInitializer { // implements CommandLineRunner
//public class QqbootApplication implements CommandLineRunner { //
    private static final Logger logger = LoggerFactory.getLogger(QqbootApplication.class);


    @Autowired
    private BotConfig botConfig;

        @Autowired
    private MessageListener messageListener;
    public static void main(String[] args) {
        SpringApplication.run(QqbootApplication.class, args);
        logger.info("\n----------------------------------------------------------\n\t" +
                "Application is running! \n\t" +
                "----------------------------------------------------------");
    }

//    @Override
    public void run(String... args) {
        String qq = botConfig.getQq();
        if (StrUtil.isBlank(qq)) {
            logger.error("QQ号为空，程序终止");
            return;
        }
        // 使用密码认证，而不是二维码认证
        Bot bot = BotFactory.INSTANCE.newBot(Long.parseLong(botConfig.getQq()), BotAuthorization.byQRCode());
        // 尝试使用安卓手机协议，通常更稳定
        bot.getConfiguration().setProtocol(BotConfiguration.MiraiProtocol.MACOS);
        bot.login();
        GlobalEventChannel.INSTANCE.registerListenerHost(messageListener);
        logger.info("QQ Bot started successfully.");

    }
}