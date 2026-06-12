package com.example.realtimechat.conversation;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        ConversationType type,
        String name,
        String avatarUrl,
        UUID createdBy,
        Instant createdAt
) {
    public static ConversationResponse from(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getName(),
                conversation.getAvatarUrl(),
                conversation.getCreatedBy().getId(),
                conversation.getCreatedAt()
        );
    }
}
