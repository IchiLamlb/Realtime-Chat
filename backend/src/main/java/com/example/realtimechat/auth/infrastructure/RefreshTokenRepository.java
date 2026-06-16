package com.example.realtimechat.auth.infrastructure;

import com.example.realtimechat.auth.domain.RefreshToken;
import com.example.realtimechat.user.domain.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken token set token.revokedAt = :revokedAt where token.user = :user and token.revokedAt is null")
    void revokeActiveTokens(User user, Instant revokedAt);
}
