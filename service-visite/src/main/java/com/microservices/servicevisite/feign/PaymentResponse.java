package com.microservices.servicevisite.feign;

import lombok.Data;

@Data
public class PaymentResponse {
private String paymentId;
private String statut;
}