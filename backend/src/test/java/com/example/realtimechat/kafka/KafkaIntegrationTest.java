package com.example.realtimechat.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.example.realtimechat.kafka.event.MessageCreatedEvent;
import com.example.realtimechat.kafka.producer.ChatEventPublisher;
import com.example.realtimechat.message.domain.MessageType;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.datasource.url=jdbc:postgresql://localhost:5433/realtime_chat?options=-c%20TimeZone=UTC")
@EmbeddedKafka(
        partitions = 1,
        topics = "chat.message.created",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@ActiveProfiles("test")
public class KafkaIntegrationTest {

    @Autowired
    private ChatEventPublisher eventPublisher;

    private static final AtomicBoolean eventConsumed = new AtomicBoolean(false);

    @KafkaListener(topics = "${app.kafka.topics.message-created}", groupId = "integration-test-group")
    public void consumeTest(MessageCreatedEvent event) {
        eventConsumed.set(true);
    }

    @Test
    public void testPublishAndConsumeMessageCreated() {
        MessageCreatedEvent event = new MessageCreatedEvent(
                UUID.randomUUID(),
                "MESSAGE_CREATED",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                MessageType.TEXT,
                "Integration test message",
                Instant.now()
        );

        eventPublisher.publishMessageCreated(event);

        await().atMost(Duration.ofSeconds(10)).untilTrue(eventConsumed);
        assertThat(eventConsumed.get()).isTrue();
    }
}
