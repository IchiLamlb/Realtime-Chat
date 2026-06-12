package com.example.realtimechat.message.api.dto;


import com.example.realtimechat.message.domain.Message;
import com.example.realtimechat.message.domain.MessageStatus;
import com.example.realtimechat.message.domain.MessageType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID conversationId,
        UUID senderId,
        MessageType type,
        String content,
        Map<String, Object> metadata,
        MessageStatus status,
        Instant createdAt
) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getType(),
                message.getContent(),
                message.getMetadata(),
                message.getStatus(),
                message.getCreatedAt()
        );
    }
}
