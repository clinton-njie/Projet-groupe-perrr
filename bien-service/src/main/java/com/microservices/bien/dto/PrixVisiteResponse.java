package com.microservices.bien.dto;

import com.microservices.bien.entity.Categorie;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrixVisiteResponse {

    private Categorie categorie;
    private Float prixVisite;
}
