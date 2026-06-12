package com.example.realtimechat.presence.api.dto;


import com.example.realtimechat.presence.domain.PresenceStatus;
import java.util.UUID;

public record PresenceResponse(
        UUID userId,
        PresenceStatus status
) {
}
