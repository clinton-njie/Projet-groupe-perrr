package com.immobilier.abonnementservice.dto.response;

import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.enums.TypeAbonnement;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class AbonnementResponse {

    private Long id;
    private Long agentId;

    private TypeAbonnement type;
    private StatutAbonnement statut;

    private BigDecimal prix;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private Long joursRestants;

    private String referenceTransaction;

}