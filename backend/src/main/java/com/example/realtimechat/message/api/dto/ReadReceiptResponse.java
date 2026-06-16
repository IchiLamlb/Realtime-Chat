package com.example.realtimechat.message.api.dto;

import com.example.realtimechat.message.domain.MessageReceipt;
import com.example.realtimechat.message.domain.MessageStatus;
import java.time.Instant;
import java.util.UUID;

public record ReadReceiptResponse(
        UUID messageId,
        UUID conversationId,
        UUID userId,
        MessageStatus status,
        Instant readAt
) {
    public static ReadReceiptResponse from(MessageReceipt receipt) {
        return new ReadReceiptResponse(
                receipt.getMessage().getId(),
                receipt.getMessage().getConversation().getId(),
                receipt.getUser().getId(),
                receipt.getStatus(),
                receipt.getCreatedAt()
        );
    }
}
