package com.immobilier.reservationservice.entity;

import com.immobilier.reservationservice.enums.OperateurMobileMoney;
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

    // agent propriétaire du bien (résolu depuis agentEmail) : propagé à ms-avis
    // via l'événement transaction.terminee pour permettre la notation de l'agent
    private Long agentId;

    private LocalDate startDate;

    private LocalDate endDate;

    // montant calculé automatiquement
    private Double amount;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    // date création réservation
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private OperateurMobileMoney operateur;

    private String telephone;

    // référence du paiement retournée par payment-service
    private String paymentId;

    // snapshot au moment de la création (utilisés pour le paiement/la notification)
    private String nomBien;

    private String nomUtilisateur;
}
