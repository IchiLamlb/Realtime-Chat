package com.example.realtimechat.analytics.api.dto;

import java.time.Instant;

public record MessagesPerMinuteResponse(
        Instant windowStart,
        Instant windowEnd,
        long messageCount
) {
}
