package com.immobilier.reservationservice.controller;

import com.immobilier.reservationservice.dto.*;

import com.immobilier.reservationservice.service.ReservationService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations") // Endpoint de base pour les opérations liées aux réservations
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @PostMapping // Endpoint pour créer une nouvelle réservation
    public ReservationResponse createReservation(

            @AuthenticationPrincipal Jwt jwt,

            @Valid
            @RequestBody
            ReservationRequest request) {

        Long userId = jwt.getClaim("userId");
        return service.createReservation(
                userId,
                request
        ); //Retourne la réponse de la réservation créée
    }

    @PutMapping("/{reservationId}/confirm")
    public ReservationResponse confirmReservation(

            @PathVariable Long reservationId) {

        return service.confirmReservation(
                reservationId
        );
    }

    @PutMapping("/{reservationId}/pay")
    public ReservationResponse initiatePayment(

            @PathVariable Long reservationId) {

        return service.initiatePayment(
                reservationId
        );
    }

    // Annulation d'une réservation
    @PutMapping("/{reservationId}/cancel")
    public ReservationResponse cancelReservation(

            @PathVariable Long reservationId) {

        return service.cancelReservation(
                reservationId
        );
    }

    @GetMapping("/{id}") // Endpoint pour récupérer une réservation par son ID
    public ReservationResponse getById(
            @PathVariable Long id) {

        return service.getReservationById(id);
    }

    @GetMapping("/user/{userId}") // Endpoint pour récupérer toutes les réservations d'un utilisateur
    public List<ReservationResponse>
    getByUser(

            @PathVariable Long userId) {

        return service.getReservationsByUser(
                userId
        );
    }
}