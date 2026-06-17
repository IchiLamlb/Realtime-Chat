package com.example.realtimechat.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class FlywayConfig {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            DataSource dataSource = flyway.getConfiguration().getDataSource();
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                // Delete the record for version 6 so Flyway will execute the script again
                statement.executeUpdate("DELETE FROM flyway_schema_history WHERE version = '6'");
            } catch (Exception e) {
                // Ignore errors if the table/row doesn't exist yet
                System.err.println("Pre-migration clean up of version 6 failed: " + e.getMessage());
            }
            flyway.repair();
            flyway.migrate();
        };
    }
}

