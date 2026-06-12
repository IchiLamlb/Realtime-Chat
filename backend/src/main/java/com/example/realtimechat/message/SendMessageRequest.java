package com.example.realtimechat.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;

public record SendMessageRequest(
        @NotNull UUID conversationId,
        @NotNull MessageType type,
        @NotBlank @Size(max = 5000) String content,
        Map<String, Object> metadata
) {
}
