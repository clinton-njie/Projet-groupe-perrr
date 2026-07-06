package com.microservices.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    @NotBlank(message = "Le nom de l'utilisateur est obligatoire")
    private String nomUtilisateur;

    @NotBlank(message = "Le nom du bien est obligatoire")
    private String nomBien;

    @NotNull(message = "La nature du paiement est obligatoire")
    private NatureType natureType;

    @NotNull(message = "Le statut est obligatoire")
    private StatutPaiement statut;

    private Double montant;
}
