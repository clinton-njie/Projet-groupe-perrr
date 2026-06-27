package com.immobilier.reservationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // Cette annotation indique que cette classe est un gestionnaire global d'exceptions pour les contrôleurs REST
public class GlobalExceptionHandler { 
    //Classe pour gérer les exceptions de manière globale et centralisée dans l'application. Elle intercepte les exceptions lancées par les contrôleurs et renvoie des réponses appropriées aux clients. 

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse>
    handleBusinessException(
            BusinessException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new ErrorResponse(
                                ex.getMessage(),
                                400
                        )
                ); //Retourne une réponse avec le message d'erreur et le code d'état HTTP 400 (Bad Request)
    }

}