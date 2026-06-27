package com.immobilier.reservationservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
//Classe pour représenter une réponse d'erreur dans l'application. Elle contient un message d'erreur et un code de statut HTTP associé.
public class ErrorResponse {

    private String message;

    private int status;
}