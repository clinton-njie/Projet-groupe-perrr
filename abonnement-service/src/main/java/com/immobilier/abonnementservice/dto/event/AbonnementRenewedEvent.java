package com.immobilier.abonnementservice.dto.event;

import lombok.*;

import java.time.LocalDate;

// Publié sur "abonnement-renewed" quand un renouvellement est activé.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonnementRenewedEvent {

    private String requestId;
    private Long abonnementId;
    private Long userId;
    private String email;
    private String type;
    private LocalDate dateDebut;
    private LocalDate dateFin;
}
