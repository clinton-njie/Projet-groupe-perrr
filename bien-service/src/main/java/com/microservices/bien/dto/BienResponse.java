package com.microservices.bien.dto;

import com.microservices.bien.entity.Categorie;
import com.microservices.bien.entity.StatutBien;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BienResponse {

    private Long id;
    private Categorie categorie;
    private String nom;
    private String ville;
    private String quartier;
    private String photoPrincipale;
    private String imagePublicId;
    private List<ImageInfo> photosSecondaires;
    private Float prixMensuel;
    private Integer nombrePiecesDisponibles;
    private LocalDate date;
    private String description;
    private String agentEmail;
    private StatutBien statut;
}
