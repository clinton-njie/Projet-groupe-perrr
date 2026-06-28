package com.immobilier.abonnementservice.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatistiqueAbonnementResponse {

    private long totalAbonnements;

    private long abonnementsActifs;
    private long abonnementsExpires;
    private long abonnementsAnnules;
    private long abonnementsEnAttente;
    private long abonnementsEchecs;

    private long abonnementsMensuels;
    private long abonnementsAnnuels;

}