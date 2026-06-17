package com.example.realtimechat.conversation.api.dto;

import jakarta.validation.constraints.Size;

public record UpdateConversationSettingsRequest(
        @Size(max = 50) String theme,
        @Size(max = 50) String backgroundColor
) {}
