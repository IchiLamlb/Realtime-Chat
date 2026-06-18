package com.example.realtimechat.config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class ClickHouseConfig {

    private static final Logger log = LoggerFactory.getLogger(ClickHouseConfig.class);

    @Bean
    JdbcTemplate clickHouseJdbcTemplate(
            @Value("${app.clickhouse.url}") String url,
            @Value("${app.clickhouse.username}") String username,
            @Value("${app.clickhouse.password}") String password
    ) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return new JdbcTemplate(dataSource);
    }

    @Bean
    CommandLineRunner clickHouseSchemaInitializer(
            JdbcTemplate clickHouseJdbcTemplate,
            @Value("classpath:clickhouse-schema.sql") Resource schemaResource
    ) {
        return args -> {
            try {
                log.info("Initializing ClickHouse schema from {}", schemaResource.getFilename());
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(schemaResource.getInputStream(), StandardCharsets.UTF_8))) {
                    String sql = reader.lines().collect(Collectors.joining("\n"));
                    String[] statements = sql.split(";");
                    for (String statement : statements) {
                        String trimmed = statement.trim();
                        if (!trimmed.isEmpty()) {
                            clickHouseJdbcTemplate.execute(trimmed);
                        }
                    }
                }
                log.info("ClickHouse schema initialized successfully.");
            } catch (Exception e) {
                log.error("Failed to initialize ClickHouse schema", e);
            }
        };
    }
}
