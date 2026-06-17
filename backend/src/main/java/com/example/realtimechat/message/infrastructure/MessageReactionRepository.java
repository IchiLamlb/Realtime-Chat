package com.example.realtimechat.message.infrastructure;

import com.example.realtimechat.message.domain.MessageReaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, UUID> {
    List<MessageReaction> findByMessageIdIn(List<UUID> messageIds);
    Optional<MessageReaction> findByMessageIdAndUserId(UUID messageId, UUID userId);
    List<MessageReaction> findByMessageId(UUID messageId);
}
