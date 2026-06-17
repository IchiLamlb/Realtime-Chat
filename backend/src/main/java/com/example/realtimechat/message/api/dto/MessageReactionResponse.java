package com.example.realtimechat.message.api.dto;

import com.example.realtimechat.message.domain.MessageReaction;
import java.util.UUID;

public record MessageReactionResponse(
    UUID userId,
    String username,
    String displayName,
    String avatarUrl,
    String emoji
) {
    public static MessageReactionResponse from(MessageReaction reaction) {
        return new MessageReactionResponse(
            reaction.getUser().getId(),
            reaction.getUser().getUsername(),
            reaction.getUser().getDisplayName(),
            reaction.getUser().getAvatarUrl(),
            reaction.getEmoji()
        );
    }
}
