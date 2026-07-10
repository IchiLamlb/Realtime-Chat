package com.example.realtimechat.notification.api.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        UUID conversationId,
        UUID messageId,
        String type,
        String content,
        Instant readAt,
        Instant createdAt
) {
}
