package com.immobilier.abonnementservice.dto.commun;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private T data;

}