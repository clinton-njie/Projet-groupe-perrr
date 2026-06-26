package com.microservices.bien.dto;

import com.microservices.bien.entity.Categorie;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BienResponse {

    private Long id;
    private Categorie categorie;
    private String nom;
    private String localisation;
    private String image;
    private Float prix;
    private LocalDate date;
    private String description;
    private Long agentId;
    private String agentEmail;
    private Boolean disponible;
}
