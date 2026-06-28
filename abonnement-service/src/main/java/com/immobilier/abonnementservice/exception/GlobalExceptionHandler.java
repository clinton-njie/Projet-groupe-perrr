package com.immobilier.abonnementservice.exception;

import com.immobilier.abonnementservice.dto.commun.ErreurResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AbonnementException.class)
    public ResponseEntity<ErreurResponse> handleAbonnement(AbonnementException ex) {

        return ResponseEntity.badRequest().body(
                ErreurResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .path("")
                        .build()
        );
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurResponse> handleNotFound(RessourceIntrouvableException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErreurResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .path("")
                        .build()
        );
    }
}