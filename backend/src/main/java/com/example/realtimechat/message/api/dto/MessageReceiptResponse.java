package com.example.realtimechat.message.api.dto;

import com.example.realtimechat.message.domain.MessageReceipt;
import com.example.realtimechat.message.domain.MessageStatus;
import java.time.Instant;
import java.util.UUID;

public record MessageReceiptResponse(
        UUID messageId,
        UUID conversationId,
        UUID userId,
        MessageStatus status,
        Instant createdAt
) {
    public static MessageReceiptResponse from(MessageReceipt receipt) {
        return new MessageReceiptResponse(
                receipt.getMessage().getId(),
                receipt.getMessage().getConversation().getId(),
                receipt.getUser().getId(),
                receipt.getStatus(),
                receipt.getCreatedAt()
        );
    }
}
