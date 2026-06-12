package com.example.realtimechat.auth;

import com.example.realtimechat.user.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {
}
