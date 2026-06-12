package com.example.realtimechat.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String messageCreatedTopic;

    public ChatEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.message-created}") String messageCreatedTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.messageCreatedTopic = messageCreatedTopic;
    }

    public void publishMessageCreated(MessageCreatedEvent event) {
        kafkaTemplate.send(messageCreatedTopic, event.conversationId().toString(), event);
    }
}
