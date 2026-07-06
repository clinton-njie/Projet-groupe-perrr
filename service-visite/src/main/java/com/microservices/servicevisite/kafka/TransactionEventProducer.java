package com.microservices.servicevisite.kafka;

import com.microservices.servicevisite.dto.TransactionTermineeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Publie sur le topic "transaction.terminee" quand une visite est TERMINE.
 * Consommé par ms-avis pour créer l'opportunité de notation.
 */
@Service
@RequiredArgsConstructor
public class TransactionEventProducer {

    public static final String TOPIC_TRANSACTION_TERMINEE = "transaction.terminee";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publier(TransactionTermineeEvent event) {
        kafkaTemplate.send(TOPIC_TRANSACTION_TERMINEE, event);
    }
}
