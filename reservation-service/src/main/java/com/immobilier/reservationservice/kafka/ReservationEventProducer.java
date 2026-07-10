package com.immobilier.reservationservice.kafka;

import com.immobilier.reservationservice.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.immobilier.reservationservice.dto.ReservationEvent;
import com.immobilier.reservationservice.dto.TransactionTermineeEvent;

@Service
@RequiredArgsConstructor
public class ReservationEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publish(ReservationEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.RK_RESERVATION_EVENTS,
                event
        );
    }

    public void publishTransactionTerminee(TransactionTermineeEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.RK_TRANSACTION_TERMINEE,
                event
        );
    }
}
