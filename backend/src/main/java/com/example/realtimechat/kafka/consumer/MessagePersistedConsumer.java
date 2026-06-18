package com.example.realtimechat.kafka.consumer;

import com.example.realtimechat.kafka.event.MessagePersistedEvent;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessagePersistedConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessagePersistedConsumer.class);
    private final StringRedisTemplate redisTemplate;

    public MessagePersistedConsumer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topics.message-persisted}", groupId = "realtime-chat-message-persisted")
    public void consume(MessagePersistedEvent event) {
        log.info("Consumed message-persisted event eventId={} messageId={} conversationId={}",
                event.eventId(), event.messageId(), event.conversationId());

        if (isDuplicateEvent(event.eventId())) {
            log.warn("Duplicate event detected, skipping: eventId={}", event.eventId());
            return;
        }

        // Process message persisted analytics or other downstream actions
        log.info("Successfully processed message persisted eventId={}", event.eventId());
    }

    private boolean isDuplicateEvent(UUID eventId) {
        String key = "processed:event:" + eventId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "true", Duration.ofDays(1));
        return !Boolean.TRUE.equals(isNew);
    }
}
