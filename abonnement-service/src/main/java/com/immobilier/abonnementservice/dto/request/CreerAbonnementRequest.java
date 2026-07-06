package com.immobilier.abonnementservice.dto.request;

import com.immobilier.abonnementservice.enums.TypeAbonnement;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreerAbonnementRequest {

    @NotNull(message = "Type abonnement obligatoire")
    private TypeAbonnement type;

    // Optionnel (ex. MOBILE_MONEY, CARTE). Transmis au paiement.
    private String modePaiement;
}
