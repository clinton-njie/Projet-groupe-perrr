package com.immobilier.reservationservice.dto.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Représente un bien tel que renvoyé par bien-service (GET /api/biens/{id})
public class PropertyDto {

    private Long id;

    private String nom;

    private Float prixMensuel;

    private String statut;

    // Email de l'agent propriétaire (exposé par bien-service) : sert à résoudre
    // l'agentId numérique pour l'événement transaction.terminee consommé par ms-avis.
    private String agentEmail;
}
