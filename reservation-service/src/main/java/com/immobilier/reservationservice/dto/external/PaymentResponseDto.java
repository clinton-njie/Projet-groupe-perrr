package com.immobilier.reservationservice.dto.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// Représente la réponse de payment-service (POST /payments)
public class PaymentResponseDto {

    private String paymentId;

    private String statut;
}
