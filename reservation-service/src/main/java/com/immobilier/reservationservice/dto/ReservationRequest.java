package com.immobilier.reservationservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReservationRequest {

    @NotNull
    private Long propertyId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;
}