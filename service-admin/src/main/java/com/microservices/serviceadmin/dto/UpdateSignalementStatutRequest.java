package com.microservices.serviceadmin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSignalementStatutRequest {

@NotBlank(message = "Le statut est obligatoire")
private String statut; // "EN_ATTENTE", "RESOLU" ou "REJETE"
}
