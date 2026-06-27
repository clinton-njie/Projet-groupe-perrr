package com.microservices.servicevisite.feign;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
private Long visitId;
private Double montant;
private String clientId;
}