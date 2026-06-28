package com.projet_immo.service_signalements.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(DailyLimitExceededException.class)
    public ResponseEntity<ApiError> handleDailyLimit(
            DailyLimitExceededException ex) {

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(AgentNotFoundException.class)
    public ResponseEntity<ApiError> handleAgentNotFound(
            AgentNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(AgentCannotReportAgentException.class)
    public ResponseEntity<ApiError> handleAgentReport(
            AgentCannotReportAgentException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(NoInteractionException.class)
    public ResponseEntity<ApiError> handleInteraction(
            NoInteractionException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("Une erreur interne est survenue."));
    }
}
