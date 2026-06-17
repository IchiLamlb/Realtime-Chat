package com.example.realtimechat.websocket.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WebRTCSignalRequest(
        @NotNull UUID conversationId,
        @NotBlank String type,
        String payload
) {
}
