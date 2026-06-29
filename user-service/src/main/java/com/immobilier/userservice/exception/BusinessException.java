package com.immobilier.userservice.exception;

// Exception métier personnalisée pour ressources non trouvées
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}