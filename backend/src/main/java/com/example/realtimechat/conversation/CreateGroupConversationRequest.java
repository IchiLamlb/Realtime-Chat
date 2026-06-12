package com.example.realtimechat.conversation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CreateGroupConversationRequest(
        @NotBlank @Size(min = 2, max = 120) String name,
        String avatarUrl,
        @NotEmpty Set<UUID> memberIds
) {
}
