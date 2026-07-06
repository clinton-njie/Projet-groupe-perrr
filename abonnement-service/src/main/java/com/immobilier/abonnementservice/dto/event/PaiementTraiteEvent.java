package com.immobilier.abonnementservice.dto.event;

import lombok.*;

// Consommé depuis le topic "paiement-traite" : résultat réel d'un paiement.
// statut attendu : SUCCESS ou ECHEC. Idempotence via requestId.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementTraiteEvent {

    private String requestId;
    private Long paiementId;
    private String statut; // SUCCESS | ECHEC
    private String reference;
    private String modePaiement;
    private String message;
}
