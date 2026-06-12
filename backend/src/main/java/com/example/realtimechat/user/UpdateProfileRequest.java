package com.example.realtimechat.user;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 100) String displayName,
        String avatarUrl,
        @Size(max = 500) String bio
) {
}
