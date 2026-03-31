package com.flexislot;

import com.flexislot.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class FlexiSlotApplication {

    private static final Logger log = LoggerFactory.getLogger(FlexiSlotApplication.class);

    public static void main(String[] args) {
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl != null) {
            log.info("[STARTUP-DIAGNOSTICS] DATABASE_URL env var is present.");
            if (dbUrl.contains("[YOUR-PASSWORD]")) {
                log.error("[STARTUP-DIAGNOSTICS] WARNING: DATABASE_URL contains placeholder '[YOUR-PASSWORD]'.");
            }
        } else {
            log.warn("[STARTUP-DIAGNOSTICS] DATABASE_URL env var is NOT set.");
        }
        
        SpringApplication.run(FlexiSlotApplication.class, args);
    }
}
