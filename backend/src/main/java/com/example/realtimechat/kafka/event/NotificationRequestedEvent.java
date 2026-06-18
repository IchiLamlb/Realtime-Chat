package com.example.realtimechat.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record NotificationRequestedEvent(
        UUID eventId,
        String eventType,
        UUID recipientId,
        UUID senderId,
        UUID messageId,
        UUID conversationId,
        String content,
        Instant occurredAt
) {
}
