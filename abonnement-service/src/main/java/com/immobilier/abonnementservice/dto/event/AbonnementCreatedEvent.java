package com.immobilier.abonnementservice.dto.event;

import lombok.*;

import java.math.BigDecimal;

// Publié sur "abonnement-created" à la création (statut EN_ATTENTE_PAIEMENT).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonnementCreatedEvent {

    private String requestId;
    private Long abonnementId;
    private Long userId;
    private String email;
    private String type;
    private BigDecimal prix;
    private String statut;
}
