package com.immobilier.userservice.entity;

import com.immobilier.userservice.enums.StatutSuppression;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// Entité représentant une demande de suppression de compte(délai : 30 jours).
@Entity
@Table(name = "suppression_compte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuppressionCompte {

    // Identifiant unique.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Utilisateur concerné par la demande de suppression.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    // Un utilisateur ne peut faire qu'une seule demande de suppression à la fois.
    private Utilisateur utilisateur;

    // Date de la demande de suppression.
    private LocalDateTime dateDemande;

    // Date prévue pour la suppression effective du compte (30 jours après la demande).
    private LocalDateTime dateSuppressionPrevue;

    // Raison de la demande de suppression.
    @Column(columnDefinition = "TEXT")
    private String motif;

    //
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutSuppression statut;

    //
    @PrePersist
    public void prePersist() {

        // Date de demande
        this.dateDemande = LocalDateTime.now();

        // Suppression prévue dans 30 jours
        this.dateSuppressionPrevue =
                LocalDateTime.now().plusDays(30);

        // Statut par défaut
        if (this.statut == null) {
            this.statut = StatutSuppression.EN_ATTENTE;
        }
    }

    // Annule la demande de suppression, remettant le statut à "ANNULÉE".
    public void annuler() {

        this.statut = StatutSuppression.ANNULEE;
    }
}


//TOKENS OVER ICI