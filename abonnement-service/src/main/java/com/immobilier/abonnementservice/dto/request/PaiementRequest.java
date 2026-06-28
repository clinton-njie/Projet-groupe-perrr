package com.immobilier.abonnementservice.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaiementRequest {

    private Long utilisateurId;
    private BigDecimal montant;
    private String description;
    private String typePaiement;

}