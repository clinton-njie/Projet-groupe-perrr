package com.immobilier.reservationservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReservationResponse {

    private Long id;

    private Long userId;

    private Long propertyId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double amount;

    private String status;

    private String nomBien;

    private String nomUtilisateur;
}
