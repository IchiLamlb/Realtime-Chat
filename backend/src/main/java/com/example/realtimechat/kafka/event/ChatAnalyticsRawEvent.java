package com.example.realtimechat.kafka.event;

import com.example.realtimechat.message.domain.MessageType;
import java.time.Instant;
import java.util.UUID;

public record ChatAnalyticsRawEvent(
        UUID eventId,
        String eventType,
        UUID conversationId,
        UUID messageId,
        UUID userId,
        MessageType messageType,
        boolean rateLimited,
        Instant occurredAt
) {
}
