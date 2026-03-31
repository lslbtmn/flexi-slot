package com.flexislot.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Custom database configuration to handle common cloud deployment issues.
 * Specifically, it ensures that the DATABASE_URL (if provided as a URI) 
 * is correctly prefixed with 'jdbc:' for the PostgreSQL driver.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String url = properties.getUrl();
        
        // Handle standard PostgreSQL URI formats (heroku/render/supabase)
        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            // Log for visibility in deployment logs
            System.out.println("Detected PostgreSQL URI, prefixing with 'jdbc:' for Spring/Hibernate compatibility.");
            properties.setUrl("jdbc:" + url);
        }

        return DataSourceBuilder.create()
                .driverClassName(properties.getDriverClassName())
                .url(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .build();
    }
}
