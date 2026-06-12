package com.example.realtimechat.kafka.consumer;


import com.example.realtimechat.kafka.event.MessageCreatedEvent;
import com.example.realtimechat.message.domain.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageCreatedConsumer.class);

    @KafkaListener(topics = "${app.kafka.topics.message-created}", groupId = "realtime-chat-message-created-log")
    public void consume(MessageCreatedEvent event) {
        log.info("Consumed message-created event eventId={} messageId={} conversationId={}",
                event.eventId(), event.messageId(), event.conversationId());
    }
}
