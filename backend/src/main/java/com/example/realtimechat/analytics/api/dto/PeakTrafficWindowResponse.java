package com.example.realtimechat.analytics.api.dto;

import java.time.Instant;

public record PeakTrafficWindowResponse(
        Instant windowStart,
        Instant windowEnd,
        long messageCount
) {
}
