package com.microservices.serviceadmin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProcessRoleRequestDto {

@NotBlank(message = "La décision est obligatoire")
private String decision; // "ACCEPTER" ou "REFUSER"

private String refusalReason;
}
