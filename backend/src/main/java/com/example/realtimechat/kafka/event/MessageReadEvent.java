package com.example.realtimechat.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record MessageReadEvent(
        UUID eventId,
        String eventType,
        UUID conversationId,
        UUID messageId,
        UUID readerId,
        Instant occurredAt
) {
}
