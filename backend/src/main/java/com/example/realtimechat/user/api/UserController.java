package com.example.realtimechat.user.api;


import com.example.realtimechat.user.api.dto.UpdateProfileRequest;
import com.example.realtimechat.user.api.dto.UserResponse;
import com.example.realtimechat.user.application.UserService;
import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.auth.application.CurrentUser;
import com.example.realtimechat.common.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CurrentUser currentUser;
    private final UserService userService;

    public UserController(CurrentUser currentUser, UserService userService) {
        this.currentUser = currentUser;
        this.userService = userService;
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> me() {
        return ApiResponse.ok("Current user", userService.getMe(currentUser.id()));
    }

    @PatchMapping("/me")
    ApiResponse<UserResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok("Profile updated", userService.updateMe(currentUser.id(), request));
    }

    @GetMapping("/search")
    ApiResponse<List<UserResponse>> search(@RequestParam String keyword) {
        return ApiResponse.ok("Users found", userService.search(keyword));
    }
}
