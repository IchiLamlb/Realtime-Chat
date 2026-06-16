package com.example.realtimechat.message.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReadMessageRequest(
        @NotNull UUID messageId
) {
}
