package com.example.realtimechat.message.infrastructure;


import com.example.realtimechat.message.domain.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByConversationIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            UUID conversationId,
            Instant joinedAt,
            Pageable pageable
    );

    List<Message> findByConversationIdAndCreatedAtBeforeAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            UUID conversationId,
            Instant cursorCreatedAt,
            Instant joinedAt,
            Pageable pageable
    );

    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    List<Message> findByConversationIdAndSenderIdNotAndCreatedAtGreaterThanEqual(
            UUID conversationId,
            UUID senderId,
            Instant joinedAt
    );
}
