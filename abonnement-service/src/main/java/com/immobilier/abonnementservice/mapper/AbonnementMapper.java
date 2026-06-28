package com.immobilier.abonnementservice.mapper;

import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.entity.Abonnement;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AbonnementMapper {

    public static AbonnementResponse toResponse(Abonnement a) {

        long joursRestants = 0;

        if (a.getDateFin() != null && a.getDateFin().isAfter(LocalDate.now())) {
            joursRestants = ChronoUnit.DAYS.between(LocalDate.now(), a.getDateFin());
        }

        return AbonnementResponse.builder()
                .id(a.getId())
                .agentId(a.getAgentId())
                .type(a.getType())
                .statut(a.getStatut())
                .prix(a.getPrix())
                .dateDebut(a.getDateDebut())
                .dateFin(a.getDateFin())
                .joursRestants(joursRestants)
                .referenceTransaction(a.getReferenceTransaction())
                .build();
    }
}