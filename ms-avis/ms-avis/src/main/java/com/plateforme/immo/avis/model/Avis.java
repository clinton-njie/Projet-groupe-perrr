package com.plateforme.immo.avis.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long opportuniteId;

    @Column(nullable = false)
    private Long clientId;

    private Long agentId;
    private Long bienId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCible typeCible;

    // Critères Agent
    private Integer noteCommunication;
    private Integer noteProfessionnalisme;
    private Integer noteDisponibilite;

    // Critères Bien
    private Integer noteConformiteAnnonce;
    private Integer noteEtatGeneral;
    private Integer noteRapportQualitePrix;

    // Critère Plateforme
    private Integer notePlateforme;

    @Column(length = 1000)
    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime dateAvis;
}