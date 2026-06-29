package com.immobilier.userservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Gestionnaire global des exceptions pour le service utilisateur, 
// permettant de centraliser le traitement des erreurs métier et de ressources non trouvées
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Exception pour ressource non trouvée
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(
            ResourceNotFoundException ex) {

        return Map.of(
                "error",
                ex.getMessage()
        );
    }

    // Exception pour erreurs métier (ex: validation, conflits, etc.)
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBusiness(
            BusinessException ex) {

        return Map.of(
                "error",
                ex.getMessage()
        );
    }
}