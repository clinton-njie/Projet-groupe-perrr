package com.immobilier.abonnementservice.dto.response;

import lombok.Getter;
import lombok.Setter;

// Accusé synchrone du paiement : {paiementId, statut = EN_ATTENTE}. Le résultat réel
// arrive plus tard via Kafka (PaiementTraiteEvent).
@Getter
@Setter
public class PaiementResponse {

    private Long paiementId;
    private String statut;
    private String reference;
    private String message;
}
