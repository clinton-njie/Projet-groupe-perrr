package com.immobilier.abonnementservice.dto.commun;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErreurResponse {

    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private String path;

}