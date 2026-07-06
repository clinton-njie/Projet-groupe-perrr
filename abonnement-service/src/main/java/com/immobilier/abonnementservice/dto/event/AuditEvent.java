package com.immobilier.abonnementservice.dto.event;

import lombok.*;

import java.time.LocalDateTime;

// Publié sur "audit" : trace des actions métier pour le service Audit.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    private String requestId;
    private String action;   // ABONNEMENT_CREE, ABONNEMENT_ACTIVE, RENOUVELLEMENT, PAIEMENT_ECHEC, ...
    private String entite;   // "ABONNEMENT"
    private Long entiteId;
    private Long userId;
    private String details;
    private LocalDateTime timestamp;
}
