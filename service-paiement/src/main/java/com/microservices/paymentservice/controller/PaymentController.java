package com.microservices.paymentservice.controller;

import com.microservices.paymentservice.dto.PaymentRequest;
import com.microservices.paymentservice.dto.PaymentResponse;
import com.microservices.paymentservice.service.ServicePaiement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

private final ServicePaiement servicePaiement;

@PostMapping
public ResponseEntity<PaymentResponse> initierPaiement(@RequestBody PaymentRequest request) {
return ResponseEntity.status(HttpStatus.CREATED).body(servicePaiement.initierPaiement(request));
}
}
