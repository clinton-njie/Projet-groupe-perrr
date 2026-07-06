package com.immobilier.reservationservice.service;

import java.util.List;

import com.immobilier.reservationservice.dto.*;

public interface ReservationService {

    // Crée la réservation et déclenche immédiatement le paiement
    ReservationResponse createReservation(
            Long userId,
            ReservationRequest request
    );

    // Callback payment-service : paiement réussi -> CONFIRME, bien -> LOUE
    ReservationResponse confirmReservation(
            Long reservationId
    );

    // Callback payment-service (échec) OU annulation manuelle avant confirmation -> ANNULE
    ReservationResponse cancelReservation(
            Long reservationId
    );

    ReservationResponse getReservationById(
            Long reservationId
    );

    List<ReservationResponse> getReservationsByUser(
            Long userId
    );

    // Interaction éligible à un signalement : réservation CONFIRME ou EXPIRE user<->agent
    boolean existeInteraction(
            Long utilisateurId,
            Long agentId
    );

    // Scheduler pour expirer les réservations anciennes
    void expireOldReservations();
}
