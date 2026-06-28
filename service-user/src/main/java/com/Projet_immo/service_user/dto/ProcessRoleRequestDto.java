package com.Projet_immo.service_user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessRoleRequestDto {

    @NotBlank(message = "La décision est obligatoire")
    private String decision; // APPROUVÉ ou REFUSÉ

    private String refusalReason;
}
