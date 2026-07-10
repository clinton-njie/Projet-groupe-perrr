package com.microservices.servicevisite.kafka;

import com.microservices.servicevisite.config.RabbitConfig;
import com.microservices.servicevisite.dto.TransactionTermineeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Publie "transaction.terminee" quand une visite est TERMINE.
 * Consommé par ms-avis pour créer l'opportunité de notation.
 */
@Service
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publier(TransactionTermineeEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.RK_TRANSACTION_TERMINEE, event);
    }
}
