package com.example.realtimechat.auth.infrastructure;

import com.example.realtimechat.auth.domain.PasswordResetToken;
import com.example.realtimechat.user.domain.User;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update PasswordResetToken token set token.usedAt = :usedAt where token.user = :user and token.usedAt is null")
    void markUnusedTokensUsed(User user, Instant usedAt);
}
