package com.immobilier.abonnementservice.kafka;

import com.immobilier.abonnementservice.config.RabbitConfig;
import com.immobilier.abonnementservice.dto.event.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

// Publie les événements métier + notification + audit. Best-effort : un souci RabbitMQ ne casse
// pas l'opération métier déjà committée en base.
@Service
@RequiredArgsConstructor
public class AbonnementEventProducer {

    private static final Logger log = LoggerFactory.getLogger(AbonnementEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    private void send(String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
        } catch (Exception e) {
            log.warn("Echec publication RabbitMQ routingKey={} : {}", routingKey, e.getMessage());
        }
    }

    public void abonnementCreated(AbonnementCreatedEvent e) {
        send(RabbitConfig.RK_ABONNEMENT_CREATED, e);
    }

    public void abonnementActivated(AbonnementActivatedEvent e) {
        send(RabbitConfig.RK_ABONNEMENT_ACTIVATED, e);
    }

    public void abonnementRenewed(AbonnementRenewedEvent e) {
        send(RabbitConfig.RK_ABONNEMENT_RENEWED, e);
    }

    public void paymentFailed(AbonnementPaymentFailedEvent e) {
        send(RabbitConfig.RK_ABONNEMENT_PAYMENT_FAILED, e);
    }

    public void notification(NotificationEvent e) {
        send(RabbitConfig.RK_NOTIFICATION, e);
    }

    public void audit(AuditEvent e) {
        send(RabbitConfig.RK_AUDIT, e);
    }
}
