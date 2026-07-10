package com.microservices.servicevisite.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ (remplace la messagerie Kafka).
 * Ce service est producteur : il publie "transaction.terminee" sur l'exchange topic partagé,
 * consommé par ms-avis pour créer l'opportunité de notation.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "immo.exchange";
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
