package com.immobilier.abonnementservice.dto.response;

import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.enums.TypeAbonnement;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class StatutAbonnementResponse {

    private boolean actif;
    private TypeAbonnement type;
    private StatutAbonnement statut;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private Long joursRestants;

}