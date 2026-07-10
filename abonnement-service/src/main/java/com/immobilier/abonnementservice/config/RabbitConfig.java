package com.immobilier.abonnementservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ (remplace l'ancien KafkaConfig).
 * Un seul exchange topic partagé par la plateforme ; les routing keys reprennent
 * les noms des anciens topics Kafka pour préserver la topologie métier.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "immo.exchange";

    // Routing keys (héritées des anciens topics Kafka)
    public static final String RK_PAIEMENT_TRAITE = "paiement-traite";
    public static final String RK_ABONNEMENT_CREATED = "abonnement-created";
    public static final String RK_ABONNEMENT_ACTIVATED = "abonnement-activated";
    public static final String RK_ABONNEMENT_RENEWED = "abonnement-renewed";
    public static final String RK_ABONNEMENT_PAYMENT_FAILED = "abonnement-payment-failed";
    public static final String RK_NOTIFICATION = "notification";
    public static final String RK_AUDIT = "audit";

    // File consommée par ce service (résultat réel du paiement)
    public static final String QUEUE_PAIEMENT_TRAITE = "abonnement.paiement-traite";

    @Bean
    public TopicExchange immoExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue paiementTraiteQueue() {
        return QueueBuilder.durable(QUEUE_PAIEMENT_TRAITE).build();
    }

    @Bean
    public Binding paiementTraiteBinding() {
        return BindingBuilder.bind(paiementTraiteQueue()).to(immoExchange()).with(RK_PAIEMENT_TRAITE);
    }

    /**
     * Sérialisation JSON. On ignore l'entête de type et on se fie au type déclaré par la
     * méthode @RabbitListener (équivalent de spring.json.use.type.headers=false côté Kafka).
     * Ce bean unique est appliqué automatiquement au RabbitTemplate et aux listeners par Spring Boot.
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper mapper = new DefaultJackson2JavaTypeMapper();
        mapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        mapper.setTrustedPackages("*");
        converter.setJavaTypeMapper(mapper);
        return converter;
    }
}
