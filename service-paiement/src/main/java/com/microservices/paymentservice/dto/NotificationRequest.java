package com.microservices.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    private String nomUtilisateur;
    private String nomBien;
    private String natureType; // RESERVATION ou VISITE
    private String statut;     // SUCCESS, FAILED ou REFUNDED
    private Double montant;
}
