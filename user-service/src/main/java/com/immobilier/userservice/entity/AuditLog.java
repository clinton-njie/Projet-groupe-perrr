package com.immobilier.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Entité représentant un journal d'audit pour les actions des utilisateurs.
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Utilisateur ayant effectué l'action.
    @ManyToOne
    private Utilisateur utilisateur;

    private String action;

    // Type de la ressource concernée par l'action.
    private String cibleType;
    // Identifiant de la ressource concernée par l'action.
    private Long cibleId;

    private LocalDateTime dateAction;

    // Détails supplémentaires sur l'action (ex: changements de champs).
    @Column(columnDefinition = "TEXT")
    private String details;

    // Enregistre la date de l'action avant insertion en base.
    @PrePersist
    public void init() {
        this.dateAction = LocalDateTime.now();
    }
}