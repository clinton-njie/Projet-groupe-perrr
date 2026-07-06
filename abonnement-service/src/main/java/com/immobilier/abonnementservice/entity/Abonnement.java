package com.immobilier.abonnementservice.entity;

import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.enums.TypeAbonnement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "abonnements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identité de l'agent (source canonique = user-service, jamais le JWT seul)
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String email;

    private String nom;

    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAbonnement type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    private String modePaiement;

    // Renseigné à l'accusé du paiement (Feign). Null tant que le paiement n'est pas initié.
    private Long paiementId;

    // Corrélation idempotente de bout en bout (demande -> paiement -> event Kafka)
    @Column(nullable = false, unique = true)
    private String requestId;

    @Column(length = 150)
    private String referenceTransaction;

    @Column(length = 255)
    private String commentaire;

    // Dates de validité : renseignées seulement à l'activation (paiement confirmé)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAbonnement statut;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
