package com.microservices.serviceadmin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteAdminRequest {

@NotBlank(message = "L'email est obligatoire")
@Email(message = "L'email doit être valide")
private String email;
}
