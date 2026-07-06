package com.microservices.servicevisite.feign;

import com.microservices.servicevisite.enums.OperateurMobileMoney;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
private Long referenceId;
private String referenceType; // "VISITE"
private String nomUtilisateur;
private String nomBien;
private Double montant;
private OperateurMobileMoney operateur;
private String telephone;
}
