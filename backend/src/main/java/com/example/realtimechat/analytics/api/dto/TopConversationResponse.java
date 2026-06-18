package com.example.realtimechat.analytics.api.dto;

import java.time.Instant;
import java.util.UUID;

public record TopConversationResponse(
        Instant windowStart,
        Instant windowEnd,
        UUID conversationId,
        long messageCount
) {
}
