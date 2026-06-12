package com.example.realtimechat.presence;

import java.util.UUID;

public record PresenceResponse(
        UUID userId,
        PresenceStatus status
) {
}
