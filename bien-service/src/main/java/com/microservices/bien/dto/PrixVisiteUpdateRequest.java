package com.microservices.bien.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrixVisiteUpdateRequest {

    @NotNull(message = "Le prix de visite est obligatoire")
    @Positive(message = "Le prix doit être positif")
    private Float prixVisite;
}
