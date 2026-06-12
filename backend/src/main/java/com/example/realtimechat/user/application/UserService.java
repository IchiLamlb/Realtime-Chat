package com.example.realtimechat.user.application;


import com.example.realtimechat.user.api.dto.UpdateProfileRequest;
import com.example.realtimechat.user.api.dto.UserResponse;
import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.user.infrastructure.UserRepository;
import com.example.realtimechat.common.error.BusinessException;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID userId) {
        return UserResponse.from(getById(userId));
    }

    @Transactional
    public UserResponse updateMe(UUID userId, UpdateProfileRequest request) {
        User user = getById(userId);
        user.updateProfile(request.displayName(), request.avatarUrl(), request.bio());
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> search(String keyword) {
        String safeKeyword = keyword == null ? "" : keyword.trim();
        if (safeKeyword.length() < 2) {
            return List.of();
        }
        return userRepository.findTop20ByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(safeKeyword, safeKeyword)
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public User getById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
    }
}
