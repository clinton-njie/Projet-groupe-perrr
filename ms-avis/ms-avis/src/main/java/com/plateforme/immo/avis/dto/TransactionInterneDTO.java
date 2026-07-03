package com.plateforme.immo.avis.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TransactionInterneDTO {
    @NotNull(message = "clientId est requis")
    private Long clientId;
    
    @NotNull(message = "agentId est requis")
    private Long agentId;
    
    @NotNull(message = "bienId est requis")
    private Long bienId;
    
    @NotNull(message = "transactionId est requis")
    private Long transactionId;
    
    @NotBlank(message = "typeTransaction est requis")
    @Pattern(regexp = "RESERVATION|VISITE", message = "Doit être RESERVATION ou VISITE")
    private String typeTransaction;
}