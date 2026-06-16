package com.example.realtimechat.message.api.dto;

import java.util.List;
import java.util.UUID;

public record MessageHistoryResponse(
        List<MessageResponse> items,
        UUID nextCursor,
        boolean hasMore
) {
}
