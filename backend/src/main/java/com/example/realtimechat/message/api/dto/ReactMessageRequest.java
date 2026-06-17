package com.example.realtimechat.message.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReactMessageRequest(
    @NotNull(message = "Message ID is required") UUID messageId,
    String emoji
) {}
