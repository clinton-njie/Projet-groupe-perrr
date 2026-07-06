package com.immobilier.reservationservice.controller;

import com.immobilier.reservationservice.client.UserClient;
import com.immobilier.reservationservice.dto.*;

import com.immobilier.reservationservice.service.ReservationService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService service;
    private final UserClient userClient;

    @PostMapping
    public ReservationResponse createReservation(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReservationRequest request) {

        // Le JWT ne porte que l'email (sujet) ; l'id numérique se résout via service-utilisateur.
        Long userId = userClient.getAuthUserIdByEmail(jwt.getSubject());
        return service.createReservation(userId, request);
    }

    // Callback payment-service : on honore le statut du paiement.
    // Un statut autre que "REUSSI" (ex. "ECHEC") annule la réservation au lieu de la confirmer.
    // Body optionnel : un appel sans corps (ou sans statut) est traité comme un succès.
    @PutMapping("/{id}/confirmer")
    public ReservationResponse confirmer(
            @PathVariable Long id,
            @RequestBody(required = false) ReservationCallbackRequest request) {

        if (request != null && request.getStatut() != null
                && !"REUSSI".equalsIgnoreCase(request.getStatut())) {
            return service.cancelReservation(id);
        }
        return service.confirmReservation(id);
    }

    // Callback payment-service (échec) ou annulation manuelle avant confirmation
    @PutMapping("/{id}/annuler")
    public ReservationResponse annuler(
            @PathVariable Long id,
            @RequestBody(required = false) ReservationCallbackRequest request) {
        return service.cancelReservation(id);
    }

    @GetMapping("/{id}")
    public ReservationResponse getById(@PathVariable Long id) {
        return service.getReservationById(id);
    }

    // Vérifie qu'un utilisateur a eu une interaction (réservation CONFIRME ou EXPIRE) avec un agent.
    // Utilisé par service-signalements pour autoriser un signalement d'agent.
    @GetMapping("/verification")
    public boolean existeInteraction(
            @RequestParam Long utilisateurId,
            @RequestParam Long agentId) {
        return service.existeInteraction(utilisateurId, agentId);
    }

    @GetMapping("/user/{userId}")
    public List<ReservationResponse> getByUser(@PathVariable Long userId) {
        return service.getReservationsByUser(userId);
    }
}
