package com.example.realtimechat.message.infrastructure;

import com.example.realtimechat.message.domain.MessageReceipt;
import com.example.realtimechat.message.domain.MessageStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageReceiptRepository extends JpaRepository<MessageReceipt, UUID> {

    Optional<MessageReceipt> findByMessage_IdAndUser_IdAndStatus(UUID messageId, UUID userId, MessageStatus status);
}
