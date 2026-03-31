package com.flexislot;

import com.flexislot.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class FlexiSlotApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlexiSlotApplication.class, args);
    }
}
