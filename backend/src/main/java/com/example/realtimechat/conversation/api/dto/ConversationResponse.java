package com.example.realtimechat.conversation.api.dto;


import com.example.realtimechat.conversation.domain.Conversation;
import com.example.realtimechat.conversation.domain.ConversationType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        ConversationType type,
        String name,
        String avatarUrl,
        UUID createdBy,
        Instant createdAt,
        List<ConversationMemberResponse> members
) {
    public static ConversationResponse from(Conversation conversation, List<ConversationMemberResponse> members) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getName(),
                conversation.getAvatarUrl(),
                conversation.getCreatedBy().getId(),
                conversation.getCreatedAt(),
                members
        );
    }
}
