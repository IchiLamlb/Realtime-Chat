package com.example.realtimechat.common.api;


import com.example.realtimechat.message.domain.Message;
import java.time.Instant;

public record ApiResponse<T>(
        Instant timestamp,
        String message,
        T data
) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(Instant.now(), message, data);
    }
}
