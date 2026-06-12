package com.example.realtimechat.message.infrastructure;


import com.example.realtimechat.message.domain.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);
}
