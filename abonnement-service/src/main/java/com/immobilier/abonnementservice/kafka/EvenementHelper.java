package com.immobilier.abonnementservice.kafka;

import com.immobilier.abonnementservice.dto.event.AuditEvent;
import com.immobilier.abonnementservice.dto.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Fabrique + publie notifications et audits (mutualisé entre les services).
@Component
@RequiredArgsConstructor
public class EvenementHelper {

    private final AbonnementEventProducer producer;

    public void notifier(String requestId, Long userId, String email, String type, String message) {
        producer.notification(NotificationEvent.builder()
                .requestId(requestId)
                .userId(userId)
                .destinataire(email)
                .type(type)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build());
    }

    public void auditer(String requestId, String action, Long entiteId, Long userId, String details) {
        producer.audit(AuditEvent.builder()
                .requestId(requestId)
                .action(action)
                .entite("ABONNEMENT")
                .entiteId(entiteId)
                .userId(userId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
