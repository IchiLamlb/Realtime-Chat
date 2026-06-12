package com.example.realtimechat.conversation;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateDirectConversationRequest(
        @NotNull UUID targetUserId
) {
}
