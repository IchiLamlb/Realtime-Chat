package com.example.realtimechat.message.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record UpdateMessageRequest(
        @NotBlank @Size(max = 5000) String content,
        Map<String, Object> metadata
) {
}
