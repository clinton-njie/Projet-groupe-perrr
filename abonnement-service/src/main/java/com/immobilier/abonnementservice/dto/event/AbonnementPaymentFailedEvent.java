package com.immobilier.abonnementservice.dto.event;

import lombok.*;

// Publié sur "abonnement-payment-failed" quand le paiement échoue.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonnementPaymentFailedEvent {

    private String requestId;
    private Long abonnementId;
    private Long userId;
    private String email;
    private String message;
}
