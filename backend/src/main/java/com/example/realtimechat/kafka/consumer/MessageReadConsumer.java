package com.example.realtimechat.kafka.consumer;

import com.example.realtimechat.kafka.event.MessageReadEvent;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageReadConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageReadConsumer.class);
    private final StringRedisTemplate redisTemplate;

    public MessageReadConsumer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topics.message-read}", groupId = "realtime-chat-message-read")
    public void consume(MessageReadEvent event) {
        log.info("Consumed message-read event eventId={} messageId={} readerId={}",
                event.eventId(), event.messageId(), event.readerId());

        if (isDuplicateEvent(event.eventId())) {
            log.warn("Duplicate event detected, skipping: eventId={}", event.eventId());
            return;
        }

        // Process read receipt analytics or secondary logic
        log.info("Successfully processed read receipt eventId={}", event.eventId());
    }

    private boolean isDuplicateEvent(UUID eventId) {
        String key = "processed:event:" + eventId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "true", Duration.ofDays(1));
        return !Boolean.TRUE.equals(isNew);
    }
}
