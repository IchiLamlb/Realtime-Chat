package com.example.realtimechat.auth.application;


import com.example.realtimechat.auth.api.dto.AuthResponse;
import com.example.realtimechat.auth.api.dto.ForgotPasswordRequest;
import com.example.realtimechat.auth.api.dto.LoginRequest;
import com.example.realtimechat.auth.api.dto.RefreshTokenRequest;
import com.example.realtimechat.auth.api.dto.RegisterRequest;
import com.example.realtimechat.auth.api.dto.ResetPasswordRequest;
import com.example.realtimechat.auth.domain.PasswordResetToken;
import com.example.realtimechat.auth.domain.RefreshToken;
import com.example.realtimechat.auth.infrastructure.PasswordResetTokenRepository;
import com.example.realtimechat.auth.infrastructure.RefreshTokenRepository;
import com.example.realtimechat.auth.security.JwtService;
import com.example.realtimechat.common.error.BusinessException;
import com.example.realtimechat.config.MailProperties;
import com.example.realtimechat.user.domain.User;
import com.example.realtimechat.user.infrastructure.UserRepository;
import com.example.realtimechat.user.api.dto.UserResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetMailService passwordResetMailService;
    private final MailProperties mailProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordResetMailService passwordResetMailService,
            MailProperties mailProperties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordResetMailService = passwordResetMailService;
        this.mailProperties = mailProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(HttpStatus.CONFLICT, "USERNAME_EXISTS", "Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "Email already exists");
        }

        User user = new User(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.displayName()
        );
        User saved = userRepository.save(user);
        return tokenResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password())
        );
        User user = userRepository.findByUsername(request.usernameOrEmail())
                .or(() -> userRepository.findByEmail(request.usernameOrEmail()))
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid credentials"));
        return tokenResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hashToken(request.refreshToken()))
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Invalid refresh token"));
        if (token.getRevokedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Invalid refresh token");
        }

        token.revoke();
        return tokenResponse(token.getUser());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String resetToken = generateSecureToken();
            passwordResetTokenRepository.markUnusedTokensUsed(user, Instant.now());
            passwordResetTokenRepository.save(new PasswordResetToken(
                    user,
                    hashToken(resetToken),
                    Instant.now().plusSeconds(mailProperties.passwordResetTokenTtlMinutes() * 60)
            ));
            passwordResetMailService.sendResetLink(user, resetToken);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hashToken(request.token()))
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_RESET_TOKEN", "Invalid password reset token"));

        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_RESET_TOKEN", "Invalid password reset token");
        }

        User user = token.getUser();
        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        token.markUsed();
        refreshTokenRepository.revokeActiveTokens(user, Instant.now());
    }

    private AuthResponse tokenResponse(User user) {
        String refreshToken = generateRefreshToken();
        RefreshToken savedRefreshToken = new RefreshToken(
                user,
                hashToken(refreshToken),
                Instant.now().plusSeconds(jwtService.refreshTokenTtlSeconds())
        );
        refreshTokenRepository.save(savedRefreshToken);
        return new AuthResponse(
                jwtService.generateAccessToken(user.getId(), user.getUsername()),
                refreshToken,
                "Bearer",
                UserResponse.from(user)
        );
    }

    private String generateRefreshToken() {
        return generateSecureToken();
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash refresh token", exception);
        }
    }
}
