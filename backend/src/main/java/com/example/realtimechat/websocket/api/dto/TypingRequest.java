package com.example.realtimechat.websocket.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TypingRequest(
        @NotNull UUID conversationId,
        boolean typing
) {
}
