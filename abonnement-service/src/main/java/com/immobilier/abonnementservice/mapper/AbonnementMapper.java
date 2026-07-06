package com.immobilier.abonnementservice.mapper;

import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.dto.response.StatutAbonnementResponse;
import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AbonnementMapper {

    private AbonnementMapper() {}

    private static long joursRestants(Abonnement a) {
        if (a.getDateFin() != null && a.getDateFin().isAfter(LocalDate.now())) {
            return ChronoUnit.DAYS.between(LocalDate.now(), a.getDateFin());
        }
        return 0;
    }

    public static AbonnementResponse toResponse(Abonnement a) {
        return AbonnementResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .email(a.getEmail())
                .nom(a.getNom())
                .prenom(a.getPrenom())
                .type(a.getType())
                .statut(a.getStatut())
                .prix(a.getPrix())
                .modePaiement(a.getModePaiement())
                .paiementId(a.getPaiementId())
                .requestId(a.getRequestId())
                .referenceTransaction(a.getReferenceTransaction())
                .commentaire(a.getCommentaire())
                .dateDebut(a.getDateDebut())
                .dateFin(a.getDateFin())
                .joursRestants(joursRestants(a))
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    public static StatutAbonnementResponse toStatut(Abonnement a) {
        boolean actif = a.getStatut() == StatutAbonnement.ACTIF
                && a.getDateFin() != null && !a.getDateFin().isBefore(LocalDate.now());
        return StatutAbonnementResponse.builder()
                .actif(actif)
                .type(a.getType())
                .statut(a.getStatut())
                .dateDebut(a.getDateDebut())
                .dateFin(a.getDateFin())
                .joursRestants(joursRestants(a))
                .build();
    }
}
