package com.example.realtimechat.auth.api;


import com.example.realtimechat.auth.api.dto.AuthResponse;
import com.example.realtimechat.auth.api.dto.LoginRequest;
import com.example.realtimechat.auth.api.dto.RegisterRequest;
import com.example.realtimechat.auth.application.AuthService;
import com.example.realtimechat.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok("Registered", authService.register(request));
    }

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Logged in", authService.login(request));
    }
}
