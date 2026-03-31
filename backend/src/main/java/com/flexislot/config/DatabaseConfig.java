package com.flexislot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Custom database configuration to handle common cloud deployment issues.
 * intercepts DataSourceProperties before any auto-configured DataSource is created,
 * ensuring the connection string is a valid JDBC URL.
 */
@Configuration
public class DatabaseConfig implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSourceProperties properties) {
            String originalUrl = properties.getUrl();
            log.info("[DATABASE-FIX] Intercepted DataSourceProperties. Configured URL: {}", 
                originalUrl != null ? (originalUrl.contains("@") ? originalUrl.substring(0, originalUrl.indexOf(":") + 3) + "***@" + originalUrl.substring(originalUrl.indexOf("@") + 1) : originalUrl) : "null");

            if (originalUrl != null) {
                String workingUrl = originalUrl;
                
                // Strip jdbc: if present to simplify URI parsing
                if (workingUrl.startsWith("jdbc:")) {
                    workingUrl = workingUrl.substring(5);
                }
                // Normalize to postgresql://
                if (workingUrl.startsWith("postgres://")) {
                    workingUrl = workingUrl.replace("postgres://", "postgresql://");
                }

                // If it's a URI style connection (contains @ and starts with postgresql://)
                if (workingUrl.startsWith("postgresql://") && workingUrl.contains("@")) {
                    try {
                        URI dbUri = new URI(workingUrl);
                        String host = dbUri.getHost();
                        int port = dbUri.getPort() == -1 ? 5432 : dbUri.getPort();
                        String path = dbUri.getPath();
                        String query = dbUri.getQuery();
                        
                        log.info("[DATABASE-FIX] Parsed URI structure. Host: {}, Port: {}, Database: {}", host, port, path);

                        // Reconstruct JDBC URL
                        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
                        if (query != null) {
                            jdbcUrl += "?" + query;
                        } else if (host != null && (host.contains("supabase") || host.contains("render"))) {
                            jdbcUrl += "?sslmode=require";
                        }
                        
                        // Override properties explicitly
                        properties.setUrl(jdbcUrl);
                        log.info("[DATABASE-FIX] Rewrote JDBC URL to: {}", jdbcUrl);

                        // Extract and override credentials
                        if (dbUri.getUserInfo() != null) {
                            String[] userInfo = dbUri.getUserInfo().split(":");
                            String extractedUser = userInfo[0];
                            properties.setUsername(extractedUser);
                            
                            if (userInfo.length > 1) {
                                String extractedPass = userInfo[1];
                                properties.setPassword(extractedPass);
                                
                                if ("[YOUR-PASSWORD]".equals(extractedPass)) {
                                    log.error("=========================================================");
                                    log.error("CRITICAL CONFIGURATION ERROR:");
                                    log.error("Your database password is literally '[YOUR-PASSWORD]'!");
                                    log.error("Please update your environment variables in Render.");
                                    log.error("=========================================================");
                                }
                            }
                        }
                        
                    } catch (URISyntaxException e) {
                        log.error("[DATABASE-FIX] Failed to parse URI: {}", e.getMessage());
                        // Simple fallback
                        if (!originalUrl.startsWith("jdbc:")) {
                            properties.setUrl("jdbc:" + originalUrl);
                        }
                    }
                } else if (!originalUrl.startsWith("jdbc:")) {
                    properties.setUrl("jdbc:" + originalUrl);
                }
            }
        }
        return bean;
    }
}
