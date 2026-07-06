package com.immobilier.reservationservice.dto;

import com.immobilier.reservationservice.enums.OperateurMobileMoney;
import jakarta.validation.constraints.NotBlank;
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

    @NotNull(message = "L'opérateur mobile money est obligatoire")
    private OperateurMobileMoney operateur;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String telephone;
}
