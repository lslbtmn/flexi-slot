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
 * Specifically, it parses URI-style connection strings (common in Render/Heroku/Supabase)
 * and converts them into standard JDBC format.
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

        // Handle standard PostgreSQL URI formats (heroku/render/supabase)
        // Format: postgresql://user:pass@host:port/dbname
        if (url != null && (url.startsWith("postgres://") || url.startsWith("postgresql://"))) {
            try {
                // Ensure the URI has a valid protocol for parsing
                String uriString = url;
                if (url.startsWith("postgres://")) {
                    uriString = url.replace("postgres://", "postgresql://");
                }
                
                URI dbUri = new URI(uriString);
                String host = dbUri.getHost();
                int port = dbUri.getPort();
                String path = dbUri.getPath();
                String query = dbUri.getQuery();
                
                // Fallback to default port if not specified
                if (port == -1) port = 5432;
                
                // Reconstruct to standard JDBC format
                url = "jdbc:postgresql://" + host + ":" + port + path;
                if (query != null) {
                    url += "?" + query;
                }

                // Extract credentials from the URI authority
                if (dbUri.getUserInfo() != null) {
                    String[] userInfo = dbUri.getUserInfo().split(":");
                    username = userInfo[0];
                    if (userInfo.length > 1) {
                        password = userInfo[1];
                    }
                }
                
                System.out.println("Parsed database URI into JDBC format: " + url);
            } catch (URISyntaxException e) {
                System.err.println("Critical: Failed to parse DATABASE_URL as URI: " + e.getMessage());
                // Fallback: just add jdbc: prefix if missing
                if (!url.startsWith("jdbc:")) {
                    url = "jdbc:" + url;
                }
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
