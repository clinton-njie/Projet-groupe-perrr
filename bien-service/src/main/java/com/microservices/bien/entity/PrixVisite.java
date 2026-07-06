package com.microservices.bien.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prix_visites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrixVisite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Categorie categorie;

    @Column(nullable = false)
    private Float prixVisite;
}
