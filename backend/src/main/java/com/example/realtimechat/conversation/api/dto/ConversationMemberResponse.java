package com.example.realtimechat.conversation.api.dto;

import com.example.realtimechat.conversation.domain.ConversationMember;
import com.example.realtimechat.conversation.domain.MemberRole;
import java.util.UUID;

public record ConversationMemberResponse(
        UUID userId,
        String username,
        String displayName,
        String avatarUrl,
        MemberRole role
) {
    public static ConversationMemberResponse from(ConversationMember member) {
        return new ConversationMemberResponse(
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getUser().getDisplayName(),
                member.getUser().getAvatarUrl(),
                member.getRole()
        );
    }
}
