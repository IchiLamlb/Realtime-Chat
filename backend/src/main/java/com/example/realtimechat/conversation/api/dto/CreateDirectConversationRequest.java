package com.example.realtimechat.conversation.api.dto;


import com.example.realtimechat.conversation.domain.Conversation;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateDirectConversationRequest(
        @NotNull UUID targetUserId
) {
}
