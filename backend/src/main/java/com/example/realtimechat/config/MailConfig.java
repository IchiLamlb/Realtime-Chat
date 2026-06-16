package com.example.realtimechat.config;

import java.util.Properties;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    JavaMailSender javaMailSender(MailProperties properties, Environment environment) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(properties.getHost());
        sender.setPort(properties.getPort());
        sender.setUsername(properties.getUsername());
        String password = environment.getProperty("MAIL_PASSWORD", properties.getPassword());
        if (password != null) {
            sender.setPassword(password.replaceAll("\\s+", ""));
        }
        sender.setProtocol(properties.getProtocol());
        sender.setDefaultEncoding(properties.getDefaultEncoding().name());

        Properties javaMailProperties = sender.getJavaMailProperties();
        javaMailProperties.putAll(properties.getProperties());
        return sender;
    }
}
