package com.example.realtimechat.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic messageCreatedTopic(@Value("${app.kafka.topics.message-created}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic messagePersistedTopic(@Value("${app.kafka.topics.message-persisted}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic presenceChangedTopic(@Value("${app.kafka.topics.presence-changed}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }
}
