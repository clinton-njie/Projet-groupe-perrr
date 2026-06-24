package com.microservices.bien.dto;

import com.microservices.bien.entity.Categorie;
import jakarta.validation.constraints.*;
import lombok.*;

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

    @NotBlank(message = "La localisation est obligatoire")
    private String localisation;

    private String image;

    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Float prix;

    @Size(max = 1000)
    private String description;
}
