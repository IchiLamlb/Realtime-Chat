package com.example.realtimechat.notification.infrastructure;

import com.example.realtimechat.notification.domain.Notification;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    Optional<Notification> findFirstByUserIdAndConversationIdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId, UUID conversationId);
    
    int countByUserIdAndConversationIdAndReadAtIsNull(UUID userId, UUID conversationId);
}
