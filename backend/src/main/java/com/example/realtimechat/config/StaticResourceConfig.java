package com.example.realtimechat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final UploadProperties uploadProperties;

    public StaticResourceConfig(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!UploadProperties.Storage.LOCAL.equals(uploadProperties.storage())) {
            return;
        }
        String uploadLocation = uploadProperties.location().toAbsolutePath().normalize().toUri().toString();
        if (!uploadLocation.endsWith("/")) {
            uploadLocation += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }
}
