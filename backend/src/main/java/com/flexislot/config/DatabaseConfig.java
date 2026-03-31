package com.flexislot.config;

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
 * Corrects URI-style connection strings (common in Render/Heroku/Supabase)
 * to standard JDBC formats, extracting credentials and ensuring SSL.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        String url = properties.getUrl();
        String username = properties.getUsername();
        String password = properties.getPassword();
        String driverClassName = properties.getDriverClassName();

        if (url != null) {
            // STEP 1: Normalize the URL for parsing
            // Handle cases where the URL might already have jdbc: prefix but still has URI syntax (user:pass@)
            String workingUrl = url;
            if (workingUrl.startsWith("jdbc:postgresql://")) {
                workingUrl = workingUrl.substring(5); // Now starts with postgresql://
            } else if (workingUrl.startsWith("postgres://")) {
                workingUrl = workingUrl.replace("postgres://", "postgresql://");
            }

            // STEP 2: Detect URI-style authority (username:password@host)
            if (workingUrl.startsWith("postgresql://") && workingUrl.contains("@")) {
                try {
                    URI dbUri = new URI(workingUrl);
                    String host = dbUri.getHost();
                    int port = dbUri.getPort();
                    String path = dbUri.getPath();
                    String query = dbUri.getQuery();

                    // STEP 3: Reconstruct JDBC URL: jdbc:postgresql://host:port/database
                    if (port == -1) port = 5432;
                    url = "jdbc:postgresql://" + host + ":" + port + path;
                    
                    // STEP 4: Handle query parameters and ensure SSL for cloud hosts
                    if (query != null) {
                        url += "?" + query;
                    } else if (host != null && (host.contains("supabase") || host.contains("render"))) {
                        // Force SSL for these providers if not explicitly defined
                        url += "?sslmode=require";
                    }

                    // STEP 5: Extract credentials
                    if (dbUri.getUserInfo() != null) {
                        String[] userInfo = dbUri.getUserInfo().split(":");
                        username = userInfo[0];
                        if (userInfo.length > 1) {
                            password = userInfo[1];
                        }
                    }
                    
                    System.out.println("System: Reconfigured production database URL successfully.");
                } catch (URISyntaxException e) {
                    System.err.println("System: Error parsing production database URI: " + e.getMessage());
                    // Fallback to simple prefixing if parsing fails
                    if (!url.startsWith("jdbc:postgresql://")) {
                        url = "jdbc:postgresql://" + url.replace("postgresql://", "").replace("postgres://", "");
                    }
                }
            } else if (!url.startsWith("jdbc:postgresql://")) {
                // If no @ is present, it's a simple host/path, just ensure the jdbc prefix is there
                url = "jdbc:postgresql://" + url.replace("postgresql://", "").replace("postgres://", "");
            }
        }

        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}
