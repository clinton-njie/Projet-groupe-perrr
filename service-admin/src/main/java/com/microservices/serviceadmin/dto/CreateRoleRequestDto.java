package com.microservices.serviceadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRoleRequestDto {

@NotNull(message = "authUserId est obligatoire")
private Long authUserId;

@NotBlank(message = "Le rôle actuel est obligatoire")
private String currentRole;

@NotBlank(message = "Le rôle demandé est obligatoire")
private String requestedRole;

@NotBlank(message = "La justification est obligatoire")
private String justification;
}
