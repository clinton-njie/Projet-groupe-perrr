package com.immobilier.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO pour la demande a etre agent par un utilisateur. Contient le motif de la demande.
@Data
public class DemandeAgentRequest {

    @NotBlank
    private String motif;
}