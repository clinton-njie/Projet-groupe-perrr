package com.plateforme.immo.avis.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_TRANSACTION_TERMINEE = "transaction.terminee";

    @Bean
    public NewTopic transactionTermineeTopic() {
        return TopicBuilder
                .name(TOPIC_TRANSACTION_TERMINEE)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
