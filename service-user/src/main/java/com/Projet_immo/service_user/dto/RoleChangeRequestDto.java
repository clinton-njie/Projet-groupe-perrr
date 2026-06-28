package com.Projet_immo.service_user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleChangeRequestDto {

    @NotBlank(message = "Le rôle demandé est obligatoire")
    private String requestedRole;

    @NotBlank(message = "La justification est obligatoire")
    private String justification;
}
