package com.example.realtimechat.config;

import java.nio.file.Path;
import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "app.upload")
public record UploadProperties(
        Path location,
        DataSize maxFileSize,
        Storage storage,
        S3 s3
) {
    public UploadProperties {
        if (location == null) {
            location = Path.of("uploads");
        }
        if (maxFileSize == null) {
            maxFileSize = DataSize.ofMegabytes(100);
        }
        if (storage == null) {
            storage = Storage.S3;
        }
        if (s3 == null) {
            s3 = new S3(null, "us-east-1", "realtime-chat-uploads", null, "minioadmin", "minioadmin", true);
        }
    }

    public enum Storage {
        LOCAL,
        S3
    }

    public record S3(
            URI endpoint,
            String region,
            String bucket,
            String publicBaseUrl,
            String accessKey,
            String secretKey,
            boolean pathStyleAccess
    ) {
        public S3 {
            if (!StringUtils.hasText(region)) {
                region = "us-east-1";
            }
            if (!StringUtils.hasText(bucket)) {
                bucket = "realtime-chat-uploads";
            }
            if (!StringUtils.hasText(accessKey)) {
                accessKey = "minioadmin";
            }
            if (!StringUtils.hasText(secretKey)) {
                secretKey = "minioadmin";
            }
        }
    }
}
