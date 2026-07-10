package com.immobilier.reservationservice.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ (remplace l'ancien KafkaConfig).
 * Ce service est producteur uniquement : il publie sur l'exchange topic partagé.
 * Les routing keys reprennent les noms des anciens topics Kafka.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "immo.exchange";

    public static final String RK_RESERVATION_EVENTS = "reservation-events";
    // Consommé par ms-avis pour créer les opportunités de notation.
    public static final String RK_TRANSACTION_TERMINEE = "transaction.terminee";

    @Bean
    public TopicExchange immoExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
