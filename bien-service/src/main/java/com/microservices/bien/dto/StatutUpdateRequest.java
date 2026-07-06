package com.microservices.bien.dto;

import com.microservices.bien.entity.StatutBien;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutUpdateRequest {

    @NotNull(message = "Le statut est obligatoire")
    private StatutBien statut;
}
