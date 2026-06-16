package com.example.realtimechat.kafka.producer;


import com.example.realtimechat.kafka.event.MessageCreatedEvent;
import com.example.realtimechat.kafka.event.MessageReadEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String messageCreatedTopic;
    private final String messageReadTopic;

    public ChatEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.message-created}") String messageCreatedTopic,
            @Value("${app.kafka.topics.message-read}") String messageReadTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.messageCreatedTopic = messageCreatedTopic;
        this.messageReadTopic = messageReadTopic;
    }

    public void publishMessageCreated(MessageCreatedEvent event) {
        kafkaTemplate.send(messageCreatedTopic, event.conversationId().toString(), event);
    }

    public void publishMessageRead(MessageReadEvent event) {
        kafkaTemplate.send(messageReadTopic, event.conversationId().toString(), event);
    }
}
