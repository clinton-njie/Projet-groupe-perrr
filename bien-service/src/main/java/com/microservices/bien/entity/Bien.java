package com.microservices.bien.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

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
    private String localisation;

    @Column(length = 500)
    private String image;

    @Column(nullable = false)
    private Float prix;

    @Builder.Default
    @Column(nullable = false)
    private LocalDate date = LocalDate.now();

    @Column(length = 1000)
    private String description;

    // ID de l'agent propriétaire du bien (vient du JWT)
    @Column(nullable = false)
    private Long agentId;

    // Email de l'agent pour référence
    @Column(nullable = false)
    private String agentEmail;

    @Builder.Default
    @Column(nullable = false)
    private Boolean disponible = true;
}
