package com.example.realtimechat.conversation.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddConversationMemberRequest(
        @NotNull UUID userId
) {
}
