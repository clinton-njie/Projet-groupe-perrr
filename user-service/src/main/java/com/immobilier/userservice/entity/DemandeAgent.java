package com.immobilier.userservice.entity;

// Import de l'énumération du statut
import com.immobilier.userservice.enums.StatutDemande;

// Imports JPA
import jakarta.persistence.*;

// Lombok
import lombok.*;

// Gestion des dates
import java.time.LocalDateTime;


@Entity
@Table(name = "demandes_agent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeAgent {

     // Identifiant unique.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Client ayant effectué la demande
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private Utilisateur utilisateur;

    //Traitement par l'administrateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Utilisateur adminTraitant;

    //Statut actuel de la demande
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statut;

    //Raison de la demande pour devenir agent
    @Column(nullable = false, columnDefinition = "TEXT")
    private String motif;

    //Motif de refus éventuel.
    @Column(columnDefinition = "TEXT")
    private String motifRefus;

    //Date de création.
    private LocalDateTime dateCreation;

    //Date de dernière modification.
    private LocalDateTime dateModification;

    //Date de traitement par l'administrateur.
    private LocalDateTime dateTraitement;

    /// Exécuté avant la première persistance(INSER) de l'entité.
    @PrePersist
    public void prePersist() {

        // Date de création
        this.dateCreation = LocalDateTime.now();

        // Date de modification
        this.dateModification = LocalDateTime.now();

        // Statut par défaut
        if (this.statut == null) {
            this.statut = StatutDemande.EN_ATTENTE;
        }
    }

    // Exécuté avant chaque mise à jour(UPDATE) de l'entité.
    @PreUpdate
    public void preUpdate() {

        this.dateModification = LocalDateTime.now();
    }
}
