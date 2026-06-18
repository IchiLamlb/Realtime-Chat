package com.example.realtimechat.kafka.event;

import com.example.realtimechat.message.domain.MessageType;
import java.time.Instant;
import java.util.UUID;

public record MessagePersistedEvent(
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
