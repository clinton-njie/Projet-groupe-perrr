package com.immobilier.abonnementservice.dto.request;

import com.immobilier.abonnementservice.enums.TypeAbonnement;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenouvelerAbonnementRequest {

    @NotNull(message = "Type abonnement obligatoire")
    private TypeAbonnement type;

    private String modePaiement;
}
