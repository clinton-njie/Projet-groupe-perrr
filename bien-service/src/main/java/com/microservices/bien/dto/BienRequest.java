package com.microservices.bien.dto;

import com.microservices.bien.entity.Categorie;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BienRequest {

    @NotNull(message = "La catégorie est obligatoire")
    private Categorie categorie;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    private String nom;

    @NotBlank(message = "La ville est obligatoire")
    private String ville;

    @NotBlank(message = "Le quartier est obligatoire")
    private String quartier;

    @NotBlank(message = "La photo principale est obligatoire")
    private String photoPrincipale;

    @Size(max = 5, message = "5 photos supplémentaires maximum")
    private List<String> photosSecondaires;

    @NotNull(message = "Le prix de réservation mensuel est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Float prixMensuel;

    // Requis uniquement si categorie == CITE (validé en service, pas ici)
    private Integer nombrePiecesDisponibles;

    @Size(max = 1000)
    private String description;
}
