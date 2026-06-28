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

    @Column(nullable = false)
    private Long agentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAbonnement type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal prix;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private StatutAbonnement statut;

    @Column(nullable = false, unique = true)
    private Long paiementId;

    @Column(length = 150)
    private String referenceTransaction;

    @Column(length = 255)
    private String commentaire;

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