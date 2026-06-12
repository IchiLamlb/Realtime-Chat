package com.example.realtimechat.user.api.dto;


import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.user.domain.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        String avatarUrl,
        String bio,
        UserStatus status,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
