package com.hudi.qqboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class QqbootApplication extends SpringBootServletInitializer {
    private static final Logger logger = LoggerFactory.getLogger(QqbootApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(QqbootApplication.class, args);
        logger.info("----------------------------------------------------------" +
                "Application is running!" +
                "----------------------------------------------------------");
    }

}