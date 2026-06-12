package com.example.realtimechat.websocket;

import java.time.Instant;
import java.util.UUID;

public record TypingEvent(
        UUID conversationId,
        UUID userId,
        boolean typing,
        Instant occurredAt
) {
}
