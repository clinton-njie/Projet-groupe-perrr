package com.immobilier.reservationservice.entity;

import com.immobilier.reservationservice.enums.ReservationStatus;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")

@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // utilisateur ayant demandé
    private Long userId;

    // bien concerné
    private Long propertyId;

    private LocalDate startDate;

    private LocalDate endDate;

    // montant calculé automatiquement
    private Double amount;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    // date création réservation
    private LocalDateTime createdAt;

    // date décision agent
    private LocalDateTime reviewedAt;

    // agent ayant traité la demande
    private Long agentId;
}