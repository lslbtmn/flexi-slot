package com.flexislot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Custom database configuration to handle common cloud deployment issues.
 * Explicitly creates the DataSource bean to guarantee that the connection
 * string is correctly parsed and formatted before Hibernate tries to connect.
 */
@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String originalUrl = properties.getUrl();
        log.info("[EXPLICIT-DB-CONFIG] Building DataSource. Initial URL: {}", 
            originalUrl != null ? (originalUrl.contains("@") ? originalUrl.substring(0, originalUrl.indexOf(":") + 3) + "***@" + originalUrl.substring(originalUrl.indexOf("@") + 1) : originalUrl) : "null");

        String url = originalUrl;
        String username = properties.getUsername();
        String password = properties.getPassword();

        if (url != null) {
            String workingUrl = url;
            
            // Strip jdbc: prefix for URI parsing
            if (workingUrl.startsWith("jdbc:")) {
                workingUrl = workingUrl.substring(5);
            }
            // Normalize postgres:// to postgresql://
            if (workingUrl.startsWith("postgres://")) {
                workingUrl = workingUrl.replace("postgres://", "postgresql://");
            }

            // If it's a URI style connection with credentials
            if (workingUrl.startsWith("postgresql://") && workingUrl.contains("@")) {
                try {
                    URI dbUri = new URI(workingUrl);
                    String host = dbUri.getHost();
                    int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
                    String path = dbUri.getPath();
                    String query = dbUri.getQuery();
                    
                    log.info("[EXPLICIT-DB-CONFIG] Parsed URI. Host: {}, Port: {}, Database: {}", host, port, path);

                    // Reconstruct JDBC URL
                    url = "jdbc:postgresql://" + host + ":" + port + path;
                    if (query != null) {
                        url += "?" + query;
                    } else if (host != null && (host.contains("supabase") || host.contains("render"))) {
                        url += "?sslmode=require";
                    }
                    
                    log.info("[EXPLICIT-DB-CONFIG] Reconstructed JDBC URL: {}", url);

                    // Extract credentials from URI to override properties
                    if (dbUri.getUserInfo() != null) {
                        String[] userInfo = dbUri.getUserInfo().split(":");
                        username = userInfo[0];
                        if (userInfo.length > 1) {
                            password = userInfo[1];
                        }
                    }
                } catch (URISyntaxException e) {
                    log.error("[EXPLICIT-DB-CONFIG] Failed to parse URI: {}", e.getMessage());
                    if (!url.startsWith("jdbc:")) {
                        url = "jdbc:" + url;
                    }
                }
            } else if (!url.startsWith("jdbc:")) {
                url = "jdbc:" + url;
            }
        }

        DataSource dataSource = DataSourceBuilder.create()
                .driverClassName(properties.getDriverClassName())
                .url(url)
                .username(username)
                .password(password)
                .build();
                
        // Diagnostic test to force connection validation right now to print the exact reason
        try {
            log.info("[EXPLICIT-DB-CONFIG] Attempting diagnostic connection...");
            dataSource.getConnection().close();
            log.info("[EXPLICIT-DB-CONFIG] Diagnostic connection SUCCESSFUL!");
        } catch (Exception e) {
            log.error("=========================================================");
            log.error("[EXPLICIT-DB-CONFIG] CRITICAL DATABASE CONNECTION FAILURE");
            log.error("Error Message: {}", e.getMessage());
            log.error("If this is a password auth failure, double check your Render env variables.");
            log.error("=========================================================");
        }

        return dataSource;
    }
}
