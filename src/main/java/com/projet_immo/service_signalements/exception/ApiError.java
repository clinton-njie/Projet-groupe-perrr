package com.projet_immo.service_signalements.exception;

import java.time.LocalDateTime;

public class ApiError {

    private LocalDateTime timestamp;
    private String message;

    public ApiError(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    // getters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
