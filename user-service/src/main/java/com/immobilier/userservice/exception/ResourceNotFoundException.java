package com.immobilier.userservice.exception;

// Exception métier personnalisée pour ressources deja existantes/utilisees/desactivées
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}