package com.immobilier.abonnementservice.kafka;

import com.immobilier.abonnementservice.config.KafkaConfig;
import com.immobilier.abonnementservice.dto.event.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// Publie les événements métier + notification + audit. Best-effort : un souci Kafka ne casse
// pas l'opération métier déjà committée en base.
@Service
@RequiredArgsConstructor
public class AbonnementEventProducer {

    private static final Logger log = LoggerFactory.getLogger(AbonnementEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private void send(String topic, String key, Object event) {
        try {
            kafkaTemplate.send(topic, key, event);
        } catch (Exception e) {
            log.warn("Echec publication Kafka topic={} key={} : {}", topic, key, e.getMessage());
        }
    }

    public void abonnementCreated(AbonnementCreatedEvent e) {
        send(KafkaConfig.TOPIC_ABONNEMENT_CREATED, e.getRequestId(), e);
    }

    public void abonnementActivated(AbonnementActivatedEvent e) {
        send(KafkaConfig.TOPIC_ABONNEMENT_ACTIVATED, e.getRequestId(), e);
    }

    public void abonnementRenewed(AbonnementRenewedEvent e) {
        send(KafkaConfig.TOPIC_ABONNEMENT_RENEWED, e.getRequestId(), e);
    }

    public void paymentFailed(AbonnementPaymentFailedEvent e) {
        send(KafkaConfig.TOPIC_ABONNEMENT_PAYMENT_FAILED, e.getRequestId(), e);
    }

    public void notification(NotificationEvent e) {
        send(KafkaConfig.TOPIC_NOTIFICATION, e.getRequestId(), e);
    }

    public void audit(AuditEvent e) {
        send(KafkaConfig.TOPIC_AUDIT, e.getRequestId(), e);
    }
}
