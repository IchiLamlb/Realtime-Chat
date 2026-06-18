package com.example.realtimechat.analytics.api.dto;

import java.time.Instant;

public record RateLimitRatioResponse(
        Instant windowStart,
        Instant windowEnd,
        long totalEvents,
        long rateLimitedEvents,
        double rateLimitRatio
) {
}
