package com.plateforme.immo.avis.config;

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
 * Ce service consomme "transaction.terminee" (publié par reservation-service et service-visite)
 * pour créer les opportunités de notation.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "immo.exchange";
    public static final String RK_TRANSACTION_TERMINEE = "transaction.terminee";
    public static final String QUEUE_TRANSACTION_TERMINEE = "ms-avis.transaction-terminee";

    @Bean
    public TopicExchange immoExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue transactionTermineeQueue() {
        return QueueBuilder.durable(QUEUE_TRANSACTION_TERMINEE).build();
    }

    @Bean
    public Binding transactionTermineeBinding() {
        return BindingBuilder.bind(transactionTermineeQueue()).to(immoExchange()).with(RK_TRANSACTION_TERMINEE);
    }

    /**
     * Sérialisation JSON. On ignore l'entête de type (les producteurs sont dans d'autres services
     * avec des classes d'événement différentes) et on se fie au type déclaré par la méthode
     * @RabbitListener — équivalent de spring.json.use.type.headers=false / value.default.type côté Kafka.
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
