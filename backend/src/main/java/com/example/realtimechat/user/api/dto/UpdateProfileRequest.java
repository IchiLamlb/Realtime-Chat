package com.example.realtimechat.user.api.dto;


import com.example.realtimechat.user.domain.User;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 100) String displayName,
        String avatarUrl,
        @Size(max = 500) String bio
) {
}
