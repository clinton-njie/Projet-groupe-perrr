package com.immobilier.reservationservice.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.immobilier.reservationservice.dto.ReservationEvent;

@Service
@RequiredArgsConstructor
public class ReservationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC =
            "reservation-events";

    public void publish(
            ReservationEvent event) {

        kafkaTemplate.send(
                TOPIC,
                event
        );
    }
}