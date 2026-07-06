package com.immobilier.abonnementservice.dto.event;

import lombok.*;

import java.time.LocalDateTime;

// Publié sur "notification" : le service Notification en fait un message utilisateur.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {

    private String requestId;
    private Long userId;
    private String destinataire; // email
    private String type;         // ABONNEMENT_CREE, PAIEMENT_RECU, ACTIVE, PAIEMENT_ECHOUE, RENOUVELLEMENT, EXPIRATION_PROCHE, EXPIRE
    private String message;
    private LocalDateTime timestamp;
}
