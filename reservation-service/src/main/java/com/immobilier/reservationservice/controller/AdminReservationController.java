package com.immobilier.reservationservice.controller;

import com.immobilier.reservationservice.dto.ReservationResponse;

import com.immobilier.reservationservice.service.ReservationService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationService service;

    @GetMapping
    public List<ReservationResponse>
    getAllReservations() {

        return service.getAllReservations();
    }
}