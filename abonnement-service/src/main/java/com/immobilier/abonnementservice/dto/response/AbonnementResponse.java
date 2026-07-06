package com.immobilier.abonnementservice.dto.response;

import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.enums.TypeAbonnement;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AbonnementResponse {

    private Long id;
    private Long userId;
    private String email;
    private String nom;
    private String prenom;

    private TypeAbonnement type;
    private StatutAbonnement statut;

    private BigDecimal prix;
    private String modePaiement;

    private Long paiementId;
    private String requestId;
    private String referenceTransaction;
    private String commentaire;

    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long joursRestants;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
