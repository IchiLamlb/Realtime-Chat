package com.example.realtimechat.kafka.consumer;

import com.example.realtimechat.kafka.event.MessageCreatedEvent;
import com.example.realtimechat.kafka.event.NotificationRequestedEvent;
import com.example.realtimechat.conversation.infrastructure.ConversationMemberRepository;
import com.example.realtimechat.conversation.domain.ConversationMember;
import com.example.realtimechat.presence.application.PresenceService;
import com.example.realtimechat.presence.domain.PresenceStatus;
import com.example.realtimechat.kafka.producer.ChatEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class MessageCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageCreatedConsumer.class);

    private final ConversationMemberRepository memberRepository;
    private final PresenceService presenceService;
    private final ChatEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;

    public MessageCreatedConsumer(
            ConversationMemberRepository memberRepository,
            PresenceService presenceService,
            ChatEventPublisher eventPublisher,
            StringRedisTemplate redisTemplate
    ) {
        this.memberRepository = memberRepository;
        this.presenceService = presenceService;
        this.eventPublisher = eventPublisher;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topics.message-created}", groupId = "realtime-chat-message-created")
    @Transactional
    public void consume(MessageCreatedEvent event) {
        log.info("Consumed message-created event eventId={} messageId={} conversationId={}",
                event.eventId(), event.messageId(), event.conversationId());

        if (isDuplicateEvent(event.eventId())) {
            log.warn("Duplicate event detected, skipping: eventId={}", event.eventId());
            return;
        }

        List<ConversationMember> members = memberRepository.findByConversationId(event.conversationId());
        
        String senderName = "Ai đó";
        for (ConversationMember member : members) {
            if (member.getUser().getId().equals(event.senderId())) {
                senderName = member.getNickname() != null ? member.getNickname() : member.getUser().getDisplayName();
                break;
            }
        }

        for (ConversationMember member : members) {
            UUID userId = member.getUser().getId();
            if (!userId.equals(event.senderId())) {
                var presence = presenceService.get(userId);
                if (PresenceStatus.OFFLINE.equals(presence.status())) {
                    log.info("Recipient user is offline, triggering notification request: userId={}", userId);
                    eventPublisher.publishNotificationRequested(new NotificationRequestedEvent(
                            UUID.randomUUID(),
                            "NEW_MESSAGE",
                            userId,
                            event.senderId(),
                            event.messageId(),
                            event.conversationId(),
                            "Bạn có tin nhắn mới từ " + senderName,
                            Instant.now()
                    ));
                }
            }
        }
    }

    private boolean isDuplicateEvent(UUID eventId) {
        String key = "processed:event:" + eventId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "true", Duration.ofDays(1));
        return !Boolean.TRUE.equals(isNew);
    }
}
