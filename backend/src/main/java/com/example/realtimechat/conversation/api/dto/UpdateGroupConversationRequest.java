package com.example.realtimechat.conversation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateGroupConversationRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        String avatarUrl
) {
}
