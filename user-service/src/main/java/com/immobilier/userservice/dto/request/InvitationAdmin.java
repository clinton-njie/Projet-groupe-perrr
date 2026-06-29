package com.immobilier.userservice.dto.request;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID; // UUID sécurisé

import com.immobilier.userservice.entity.Utilisateur;

// Entité représentant une invitation pour devenir administrateur.  
@Entity
@Table(name = "invitations_admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitationAdmin {

    // Identifiant unique.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   // Adresse email de l'invité.
    @Column(nullable = false, unique = true)
    private String email;

    // Token d'invitation sécurisé.
    @Column(nullable = false, unique = true)
    private String token;

    // Super administrateur ayant créé l'invitation.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cree_par",
            nullable = false
    )
    private Utilisateur creePar;

    // Date d'expiration de l'invitation.
    @Column(nullable = false)
    private LocalDateTime expireLe;

    // Indique si l'invitation a été utilisée.
    @Builder.Default
    private Boolean utilisee = false;

    // Date de création de l'invitation.
    private LocalDateTime dateCreation;

    // Date de dernière modification de l'invitation.
    @PrePersist
    public void prePersist() {

        // Date de création
        this.dateCreation = LocalDateTime.now();

        // Génération automatique du token
        if (this.token == null) {
            this.token = UUID.randomUUID().toString();
        }

        // Expiration dans 7 jours
        if (this.expireLe == null) {
            this.expireLe = LocalDateTime.now().plusDays(7);
        }
    }

    //
    public boolean estValide() {

        return !utilisee
                && expireLe.isAfter(LocalDateTime.now());
    }
}