package com.example.realtimechat.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.upload")
public record UploadProperties(
        Path location,
        DataSize maxFileSize
) {
    public UploadProperties {
        if (location == null) {
            location = Path.of("uploads");
        }
        if (maxFileSize == null) {
            maxFileSize = DataSize.ofMegabytes(100);
        }
    }
}
