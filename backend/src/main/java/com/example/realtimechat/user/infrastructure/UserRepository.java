package com.example.realtimechat.user.infrastructure;


import com.example.realtimechat.user.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findTop20ByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
            String username,
            String email,
            String displayName
    );
}
