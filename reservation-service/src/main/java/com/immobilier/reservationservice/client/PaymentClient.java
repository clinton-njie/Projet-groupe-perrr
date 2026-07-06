package com.immobilier.reservationservice.client;

import com.immobilier.reservationservice.dto.external.PaymentRequestDto;
import com.immobilier.reservationservice.dto.external.PaymentResponseDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/payments")
    PaymentResponseDto initiatePayment(
            PaymentRequestDto request
    );
}
