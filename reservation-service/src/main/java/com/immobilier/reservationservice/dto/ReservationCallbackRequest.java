package com.immobilier.reservationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Corps du callback envoyé par payment-service sur /confirmer et /annuler.
// statut = "REUSSI" (paiement direct réussi) ou "ECHEC".
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCallbackRequest {
    private String statut;
    private String paymentId;
}
