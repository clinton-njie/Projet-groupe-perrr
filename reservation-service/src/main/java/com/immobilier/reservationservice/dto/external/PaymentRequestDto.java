package com.immobilier.reservationservice.dto.external;

import com.immobilier.reservationservice.enums.OperateurMobileMoney;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Contrat unifié attendu par payment-service (POST /payments)
public class PaymentRequestDto {
    private Long referenceId;
    private String referenceType; // "RESERVATION"
    private String nomUtilisateur;
    private String nomBien;
    private Double montant;
    private OperateurMobileMoney operateur;
    private String telephone;
}
