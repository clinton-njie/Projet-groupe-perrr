package com.immobilier.abonnementservice.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaiementRequest {

    // Corrélation idempotente : renvoyée telle quelle dans le PaiementTraiteEvent
    private String requestId;

    private Long utilisateurId;
    private String email;
    private BigDecimal montant;
    private String description;
    private String typePaiement;
    private String modePaiement;
}
