package com.example.realtimechat.common.api;


import com.example.realtimechat.message.domain.Message;
import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String traceId
) {
}
