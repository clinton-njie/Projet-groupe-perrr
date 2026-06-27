package com.microservices.servicevisite.dto;

import lombok.Data;

@Data
public class PaymentCallbackRequest {
private String paymentId;
private String statut; // "REUSSI" ou "ECHEC"
}
