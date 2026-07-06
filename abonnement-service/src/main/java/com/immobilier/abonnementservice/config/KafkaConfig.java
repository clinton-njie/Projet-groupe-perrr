package com.immobilier.abonnementservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_PAIEMENT_TRAITE = "paiement-traite";
    public static final String TOPIC_ABONNEMENT_CREATED = "abonnement-created";
    public static final String TOPIC_ABONNEMENT_ACTIVATED = "abonnement-activated";
    public static final String TOPIC_ABONNEMENT_RENEWED = "abonnement-renewed";
    public static final String TOPIC_ABONNEMENT_PAYMENT_FAILED = "abonnement-payment-failed";
    public static final String TOPIC_NOTIFICATION = "notification";
    public static final String TOPIC_AUDIT = "audit";

    private NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(3).replicas(1).build();
    }

    @Bean NewTopic paiementTraiteTopic() { return topic(TOPIC_PAIEMENT_TRAITE); }
    @Bean NewTopic abonnementCreatedTopic() { return topic(TOPIC_ABONNEMENT_CREATED); }
    @Bean NewTopic abonnementActivatedTopic() { return topic(TOPIC_ABONNEMENT_ACTIVATED); }
    @Bean NewTopic abonnementRenewedTopic() { return topic(TOPIC_ABONNEMENT_RENEWED); }
    @Bean NewTopic abonnementPaymentFailedTopic() { return topic(TOPIC_ABONNEMENT_PAYMENT_FAILED); }
    @Bean NewTopic notificationTopic() { return topic(TOPIC_NOTIFICATION); }
    @Bean NewTopic auditTopic() { return topic(TOPIC_AUDIT); }
}
