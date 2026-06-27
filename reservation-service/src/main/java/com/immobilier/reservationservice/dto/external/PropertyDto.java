package com.immobilier.reservationservice.dto.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//Classe permettant de représenter une propriété dans le contexte de la réservation
//DTO(Data Transfer Object) permet de transférer les données de la propriété entre les différentes couches de l'application
public class PropertyDto {

    private Long id;

    private String title;

    private Double dailyPrice;

    private Boolean available;
}