package com.immobilier.reservationservice.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    //Classe représentant la requête de paiement envoyée au service de paiement externe
    private Long userId;
    private Long reservationId;
    private Long propertyId;
    private Double amount;
}