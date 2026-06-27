package com.immobilier.reservationservice.client;

import com.immobilier.reservationservice.dto.external.PaymentRequestDto;
import com.immobilier.reservationservice.dto.external.PaymentResponseDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "PAYMENT-SERVICE") // Nom du service de paiement pour la communication via Feign(dans la gateway,le processus de paiement est exposé sous le nom "PAIEMENT-SERVICE")
public interface PaymentClient {

    @PostMapping("/api/paiements") 
    PaymentResponseDto initiatePayment(
            PaymentRequestDto request
    );
}