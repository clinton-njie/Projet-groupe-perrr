package com.immobilier.abonnementservice.dto.event;

import lombok.*;

import java.time.LocalDate;

// Publié sur "abonnement-activated" après confirmation du paiement.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonnementActivatedEvent {

    private String requestId;
    private Long abonnementId;
    private Long userId;
    private String email;
    private String type;
    private LocalDate dateDebut;
    private LocalDate dateFin;
}
