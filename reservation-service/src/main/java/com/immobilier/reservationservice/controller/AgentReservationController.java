package com.immobilier.reservationservice.controller;

import com.immobilier.reservationservice.dto.ReservationResponse;

import com.immobilier.reservationservice.service.ReservationService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent/reservations")
@RequiredArgsConstructor
public class AgentReservationController {

    private final ReservationService service;

    @GetMapping("/pending") // Endpoint pour récupérer toutes les réservations en attente
    public List<ReservationResponse>
    pendingReservations() {

        return service.getPendingReservations();
    }

    @GetMapping("/me/{agentId}") //Endp
    public List<ReservationResponse>
    myReservations(
            @PathVariable Long agentId) {

        return service
                .getReservationsByAgent(
                        agentId
                );
    }

    @PutMapping("/{id}/approve")//End
    public ReservationResponse approve(

            @PathVariable Long id,

            @RequestParam Long agentId) {

        return service.approve(
                id,
                agentId
        );
    }

    @PutMapping("/{id}/reject")
    public ReservationResponse reject(

            @PathVariable Long id,

            @RequestParam Long agentId) {

        return service.reject(
                id,
                agentId
        );
    }
}