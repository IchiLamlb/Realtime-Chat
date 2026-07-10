package com.example.realtimechat.kafka.consumer;

import com.example.realtimechat.kafka.event.NotificationRequestedEvent;
import com.example.realtimechat.notification.domain.Notification;
import com.example.realtimechat.notification.infrastructure.NotificationRepository;
import com.example.realtimechat.user.infrastructure.UserRepository;
import com.example.realtimechat.conversation.infrastructure.ConversationRepository;
import com.example.realtimechat.message.infrastructure.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.UUID;

@Component
public class NotificationRequestedConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationRequestedConsumer.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final StringRedisTemplate redisTemplate;

    public NotificationRequestedConsumer(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            StringRedisTemplate redisTemplate
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topics.notification-requested}", groupId = "realtime-chat-notification-requested")
    @Transactional
    public void consume(NotificationRequestedEvent event) {
        log.info("Consumed notification-requested event eventId={} recipientId={}", event.eventId(), event.recipientId());

        if (isDuplicateEvent(event.eventId())) {
            log.warn("Duplicate event detected, skipping: eventId={}", event.eventId());
            return;
        }

        var recipient = userRepository.findById(event.recipientId()).orElse(null);
        if (recipient == null) {
            log.warn("Recipient user not found: {}", event.recipientId());
            return;
        }

        var conversation = conversationRepository.findById(event.conversationId()).orElse(null);
        var message = messageRepository.findById(event.messageId()).orElse(null);

        // Gom notification
        var existingOpt = notificationRepository.findFirstByUserIdAndConversationIdAndReadAtIsNullOrderByCreatedAtDesc(
                event.recipientId(), event.conversationId());
                
        if (existingOpt.isPresent()) {
            Notification oldNotif = existingOpt.get();
            notificationRepository.delete(oldNotif);
        }
        
        int unreadCount = notificationRepository.countByUserIdAndConversationIdAndReadAtIsNull(event.recipientId(), event.conversationId()) + 1;
        String content = event.content();
        if (unreadCount > 1) {
            content = "Bạn có " + unreadCount + " tin nhắn mới chưa đọc";
        }

        Notification notification = new Notification(
                recipient,
                conversation,
                message,
                event.eventType(),
                content
        );
        notificationRepository.save(notification);
        log.info("Saved notification for offline user display_name={}", recipient.getDisplayName());
    }

    private boolean isDuplicateEvent(UUID eventId) {
        String key = "processed:event:" + eventId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "true", Duration.ofDays(1));
        return !Boolean.TRUE.equals(isNew);
    }
}
