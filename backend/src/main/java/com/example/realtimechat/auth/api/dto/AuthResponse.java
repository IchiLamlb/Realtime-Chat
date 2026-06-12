package com.example.realtimechat.auth.api.dto;


import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.user.api.dto.UserResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserResponse user
) {
}
