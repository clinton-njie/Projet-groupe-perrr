package com.plateforme.immo.avis.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AvisPlatformeDTO {
    @NotNull @Min(1) @Max(5)
    private Integer notePlateforme;

    @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
    private String commentaire;
}