package com.example.realtimechat.kafka.producer;


import com.example.realtimechat.kafka.event.MessageCreatedEvent;
import com.example.realtimechat.kafka.event.ChatAnalyticsRawEvent;
import com.example.realtimechat.kafka.event.MessageReadEvent;
import com.example.realtimechat.kafka.event.MessagePersistedEvent;
import com.example.realtimechat.kafka.event.NotificationRequestedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String messageCreatedTopic;
    private final String messageReadTopic;
    private final String messagePersistedTopic;
    private final String notificationRequestedTopic;
    private final String analyticsRawTopic;

    public ChatEventPublisher(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.message-created}") String messageCreatedTopic,
            @Value("${app.kafka.topics.message-read}") String messageReadTopic,
            @Value("${app.kafka.topics.message-persisted}") String messagePersistedTopic,
            @Value("${app.kafka.topics.notification-requested}") String notificationRequestedTopic,
            @Value("${app.kafka.topics.analytics-raw}") String analyticsRawTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.messageCreatedTopic = messageCreatedTopic;
        this.messageReadTopic = messageReadTopic;
        this.messagePersistedTopic = messagePersistedTopic;
        this.notificationRequestedTopic = notificationRequestedTopic;
        this.analyticsRawTopic = analyticsRawTopic;
    }

    public void publishMessageCreated(MessageCreatedEvent event) {
        kafkaTemplate.send(messageCreatedTopic, event.conversationId().toString(), event);
    }

    public void publishMessageRead(MessageReadEvent event) {
        kafkaTemplate.send(messageReadTopic, event.conversationId().toString(), event);
    }

    public void publishMessagePersisted(MessagePersistedEvent event) {
        kafkaTemplate.send(messagePersistedTopic, event.conversationId().toString(), event);
    }

    public void publishNotificationRequested(NotificationRequestedEvent event) {
        kafkaTemplate.send(notificationRequestedTopic, event.recipientId().toString(), event);
    }

    public void publishAnalyticsRaw(ChatAnalyticsRawEvent event) {
        kafkaTemplate.send(analyticsRawTopic, event.userId().toString(), event);
    }
}
