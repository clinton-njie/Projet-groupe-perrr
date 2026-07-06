package com.immobilier.reservationservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.immobilier.reservationservice.dto.ReservationEvent;
import com.immobilier.reservationservice.dto.TransactionTermineeEvent;

@Service
@RequiredArgsConstructor
public class ReservationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC =
            "reservation-events";

    // Topic consommé par ms-avis (et ms-visite) pour créer les opportunités de notation.
    private static final String TOPIC_TRANSACTION_TERMINEE =
            "transaction.terminee";

    public void publish(
            ReservationEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event
        );
    }

    public void publishTransactionTerminee(
            TransactionTermineeEvent event) {

        kafkaTemplate.send(
                TOPIC_TRANSACTION_TERMINEE,
                event
        );
    }
}