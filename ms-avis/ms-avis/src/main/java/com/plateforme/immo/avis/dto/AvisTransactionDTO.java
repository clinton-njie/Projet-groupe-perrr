package com.plateforme.immo.avis.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AvisTransactionDTO {
    @NotNull(message = "L'ID de l'opportunité est requis")
    private Long opportuniteId;

    @NotNull @Min(1) @Max(5)
    private Integer noteCommunication;

    @NotNull @Min(1) @Max(5)
    private Integer noteProfessionnalisme;

    @NotNull @Min(1) @Max(5)
    private Integer noteDisponibilite;

    @NotNull @Min(1) @Max(5)
    private Integer noteConformiteAnnonce;

    @NotNull @Min(1) @Max(5)
    private Integer noteEtatGeneral;

    @NotNull @Min(1) @Max(5)
    private Integer noteRapportQualitePrix;

    @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
    private String commentaire;
}