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

    // URL Cloudinary (secure_url) de la photo principale
    @Column(nullable = false, length = 500)
    private String photoPrincipale;

    // public_id Cloudinary de la photo principale, pour pouvoir la supprimer
    @Column(length = 255)
    private String imagePublicId;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "bien_photos_secondaires", joinColumns = @JoinColumn(name = "bien_id"))
    private List<ImageBien> photosSecondaires = new ArrayList<>();

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
