package com.microservices.serviceadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AcceptInviteRequest {

@NotBlank(message = "Le mot de passe est obligatoire")
@Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
private String password;

private String firstname;

private String lastname;
}
