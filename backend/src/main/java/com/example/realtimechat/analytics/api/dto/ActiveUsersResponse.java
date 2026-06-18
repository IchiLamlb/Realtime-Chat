package com.example.realtimechat.analytics.api.dto;

import java.time.Instant;

public record ActiveUsersResponse(
        Instant windowStart,
        Instant windowEnd,
        long activeUsers
) {
}
