package com.microservices.bien.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "biens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categorie categorie;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 255)
    private String ville;

    @Column(nullable = false, length = 255)
    private String quartier;

    @Column(nullable = false, length = 500)
    private String photoPrincipale;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "bien_photos_secondaires", joinColumns = @JoinColumn(name = "bien_id"))
    @Column(name = "url", length = 500)
    private List<String> photosSecondaires = new ArrayList<>();

    @Column(nullable = false)
    private Float prixMensuel;

    // Uniquement renseigné (et requis) pour la catégorie CITE
    private Integer nombrePiecesDisponibles;

    @Builder.Default
    @Column(nullable = false)
    private LocalDate date = LocalDate.now();

    @Column(length = 1000)
    private String description;

    // Email de l'agent propriétaire (vient du JWT) — identifiant fiable du propriétaire
    @Column(nullable = false)
    private String agentEmail;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private StatutBien statut = StatutBien.DISPONIBLE;
}
