package com.example.realtimechat.message.application;

import com.example.realtimechat.common.error.BusinessException;
import com.example.realtimechat.config.UploadProperties;
import java.io.IOException;
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

@Service
public class AttachmentStorageService {

    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final Path rootLocation;
    private final long maxFileSize;

    public AttachmentStorageService(UploadProperties uploadProperties) {
        this.rootLocation = uploadProperties.location().toAbsolutePath().normalize();
        this.maxFileSize = uploadProperties.maxFileSize().toBytes();
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

        String relativeDirectory = LocalDate.now().format(DATE_PATH);
        String storedName = UUID.randomUUID() + extension(originalName);
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

        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        String url = "/uploads/" + relativeDirectory + "/" + storedName;
        return new StoredAttachment(originalName, storedName, url, contentType, file.getSize(), isImage(contentType));
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
