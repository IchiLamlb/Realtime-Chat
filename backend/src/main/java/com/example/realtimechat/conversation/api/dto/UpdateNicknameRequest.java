package com.example.realtimechat.conversation.api.dto;

import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(
        @Size(max = 100) String nickname
) {}
