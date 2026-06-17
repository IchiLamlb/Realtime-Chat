package com.example.realtimechat.websocket.api.dto;

import java.time.Instant;
import java.util.UUID;

public record WebRTCSignalEvent(
        UUID conversationId,
        UUID senderId,
        String type,
        String payload,
        Instant timestamp
) {
}
