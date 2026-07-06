package com.immobilier.abonnementservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class StatistiqueAbonnementResponse {

    private long totalAbonnements;

    private long abonnementsActifs;
    private long abonnementsExpires;
    private long abonnementsAnnules;
    private long abonnementsEnAttente;
    private long abonnementsEchecsPaiement;

    private long abonnementsMensuels;
    private long abonnementsAnnuels;

    // Renseignés uniquement pour les stats par période
    private LocalDate debutPeriode;
    private LocalDate finPeriode;
    private Long totalSurPeriode;
}
