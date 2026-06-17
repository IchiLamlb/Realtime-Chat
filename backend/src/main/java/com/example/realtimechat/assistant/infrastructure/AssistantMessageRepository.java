package com.example.realtimechat.assistant.infrastructure;

import com.example.realtimechat.assistant.domain.AssistantMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantMessageRepository extends JpaRepository<AssistantMessage, UUID> {
    List<AssistantMessage> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
