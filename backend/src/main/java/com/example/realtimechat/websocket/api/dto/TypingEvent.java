package com.example.realtimechat.websocket.api.dto;

import java.time.Instant;
import java.util.UUID;

public record TypingEvent(
        UUID conversationId,
        UUID userId,
        boolean typing,
        Instant occurredAt
) {
}
