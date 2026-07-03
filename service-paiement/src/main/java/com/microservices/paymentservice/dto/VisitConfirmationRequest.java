package com.microservices.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitConfirmationRequest {
private String statut; // "REUSSI" ou "ECHEC"
private String paymentId;
}
