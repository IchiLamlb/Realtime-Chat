package com.example.realtimechat.message.application;

import com.example.realtimechat.common.error.BusinessException;
import com.example.realtimechat.config.UploadProperties;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class AttachmentStorageService {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final UploadProperties uploadProperties;
    private final Path rootLocation;
    private final long maxFileSize;
    private final S3Client s3Client;

    public AttachmentStorageService(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
        this.rootLocation = uploadProperties.location().toAbsolutePath().normalize();
        this.maxFileSize = uploadProperties.maxFileSize().toBytes();
        this.s3Client = UploadProperties.Storage.S3.equals(uploadProperties.storage()) ? createS3Client(uploadProperties.s3()) : null;
    }

    public StoredAttachment store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ATTACHMENT_REQUIRED", "Attachment file is required");
        }
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(HttpStatus.PAYLOAD_TOO_LARGE, "ATTACHMENT_TOO_LARGE", "Attachment must be 100MB or smaller");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename());
        if (originalName.contains("..")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ATTACHMENT_NAME", "Attachment filename is invalid");
        }

        String storedName = UUID.randomUUID() + extension(originalName);
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();

        if (UploadProperties.Storage.S3.equals(uploadProperties.storage())) {
            return storeInS3(file, originalName, storedName, contentType);
        }

        return storeLocally(file, originalName, storedName, contentType);
    }

    @PreDestroy
    void close() {
        if (s3Client != null) {
            s3Client.close();
        }
    }

    private StoredAttachment storeInS3(MultipartFile file, String originalName, String storedName, String contentType) {
        String objectKey = "attachments/" + LocalDate.now().format(DATE_PATH) + "/" + storedName;
        UploadProperties.S3 s3 = uploadProperties.s3();

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(s3.bucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | SdkException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "ATTACHMENT_STORE_FAILED", "Could not store attachment");
        }

        return new StoredAttachment(originalName, objectKey, publicUrl(s3, objectKey), contentType, file.getSize(), isImage(contentType));
    }

    private StoredAttachment storeLocally(MultipartFile file, String originalName, String storedName, String contentType) {
        String relativeDirectory = LocalDate.now().format(DATE_PATH);
        Path targetDirectory = rootLocation.resolve(relativeDirectory).normalize();
        Path targetFile = targetDirectory.resolve(storedName).normalize();
        if (!targetFile.startsWith(rootLocation)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ATTACHMENT_PATH", "Attachment path is invalid");
        }

        try {
            Files.createDirectories(targetDirectory);
            file.transferTo(targetFile);
        } catch (IOException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "ATTACHMENT_STORE_FAILED", "Could not store attachment");
        }

        String url = "/uploads/" + relativeDirectory + "/" + storedName;
        return new StoredAttachment(originalName, storedName, url, contentType, file.getSize(), isImage(contentType));
    }

    private S3Client createS3Client(UploadProperties.S3 s3) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(s3.region()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(s3.accessKey(), s3.secretKey())))
                .forcePathStyle(s3.pathStyleAccess());
        if (s3.endpoint() != null) {
            builder.endpointOverride(s3.endpoint());
        }
        return builder.build();
    }

    private String publicUrl(UploadProperties.S3 s3, String objectKey) {
        if (StringUtils.hasText(s3.publicBaseUrl())) {
            return trimTrailingSlash(s3.publicBaseUrl()) + "/" + objectKey;
        }
        URI endpoint = s3.endpoint();
        if (endpoint != null) {
            return trimTrailingSlash(endpoint.toString()) + "/" + s3.bucket() + "/" + objectKey;
        }
        return "https://" + s3.bucket() + ".s3." + s3.region() + ".amazonaws.com/" + objectKey;
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String extension(String filename) {
        String extension = StringUtils.getFilenameExtension(filename);
        if (!StringUtils.hasText(extension)) {
            return "";
        }
        return "." + extension.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private boolean isImage(String contentType) {
        return contentType.toLowerCase(Locale.ROOT).startsWith("image/");
    }
}
