package com.immobilier.reservationservice.dto.external;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponseDto {
    //Classe représentant la réponse de paiement reçue du service de paiement externe

    private boolean success;

    private String transactionId;

    private String message;
}