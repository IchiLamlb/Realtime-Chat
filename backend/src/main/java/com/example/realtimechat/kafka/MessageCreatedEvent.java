package com.example.realtimechat.kafka;

import com.example.realtimechat.message.MessageType;
import java.time.Instant;
import java.util.UUID;

public record MessageCreatedEvent(
        UUID eventId,
        String eventType,
        UUID conversationId,
        UUID messageId,
        UUID senderId,
        MessageType messageType,
        String content,
        Instant occurredAt
) {
}
