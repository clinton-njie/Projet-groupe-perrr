package com.microservices.paymentservice.controller;

import com.microservices.paymentservice.dto.PaymentRequest;
import com.microservices.paymentservice.dto.PaymentResponse;
import com.microservices.paymentservice.service.ServicePaiement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final ServicePaiement servicePaiement;

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> initierPaiement(@RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicePaiement.initierPaiement(request));
    }

    // Appelé par visite-service quand l'agent termine la visite ou constate l'absence du client
    @PutMapping("/api/paiements/{id}/capturer")
    public ResponseEntity<PaymentResponse> capturer(@PathVariable Long id) {
        return ResponseEntity.ok(servicePaiement.capturer(id));
    }

    // Appelé par visite-service lors d'une annulation remboursable (>=24h ou annulation agent)
    @PutMapping("/api/paiements/{id}/rembourser")
    public ResponseEntity<PaymentResponse> rembourser(@PathVariable Long id) {
        return ResponseEntity.ok(servicePaiement.rembourser(id));
    }
}
