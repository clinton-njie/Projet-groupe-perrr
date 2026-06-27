package com.immobilier.reservationservice.service;

import java.util.List;

import com.immobilier.reservationservice.dto.*;

public interface ReservationService {

    // Methode pour créer une réservation
    ReservationResponse createReservation(
            Long userId,
            ReservationRequest request
    );
   
    // Methode pour approuver une réservation par l'agent
    ReservationResponse approve(
            Long reservationId,
            Long agentId
    );

    // Methode pour rejeter une réservation par l'agent
    ReservationResponse reject(
            Long reservationId,
            Long agentId
    );

    // Annule une réservation
    ReservationResponse cancelReservation(
            Long reservationId
    );

    // Confirmation de la réservation après paiement réussi
    ReservationResponse confirmReservation(
        Long reservationId
    );

        // Déclenche le processus de paiement
        ReservationResponse initiatePayment(
                Long reservationId
        );


    //  Methode pour récupérer une réservation par son ID
    ReservationResponse getReservationById(
            Long reservationId
    );

    //Methode pour récupérer toutes les réservations d'un agent
    List<ReservationResponse> getReservationsByAgent(
        Long agentId
    );

    // Methode pour récupérer toutes les réservations d'un utilisateur
    List<ReservationResponse> getReservationsByUser(
            Long userId
    );

    // Methode pour récupérer toutes les réservations en attente d'approbation par l'agent
    List<ReservationResponse> getPendingReservations();


    // Meth
    List<ReservationResponse> getAllReservations();

    // Scheduler pour expirer les réservations anciennes
    void expireOldReservations();
}