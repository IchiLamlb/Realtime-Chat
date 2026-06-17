package com.example.realtimechat.message.application;

public record StoredAttachment(
        String originalName,
        String storedName,
        String url,
        String contentType,
        long size,
        boolean image
) {
}
