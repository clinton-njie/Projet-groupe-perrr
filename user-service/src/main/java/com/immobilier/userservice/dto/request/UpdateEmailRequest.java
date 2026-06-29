package com.immobilier.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO pour la mise à jour de l'email d'un utilisateur. 
// Contient le nouvel email à mettre à jour.
@Data
public class UpdateEmailRequest {

    @NotBlank
    @Email
    private String email;
}