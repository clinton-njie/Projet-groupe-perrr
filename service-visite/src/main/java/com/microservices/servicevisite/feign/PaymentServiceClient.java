package com.microservices.servicevisite.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

@PostMapping("/payments")
PaymentResponse initierPaiement(@RequestBody PaymentRequest request);

@PutMapping("/api/paiements/{id}/capturer")
PaymentResponse capturer(@PathVariable("id") Long id);

@PutMapping("/api/paiements/{id}/rembourser")
PaymentResponse rembourser(@PathVariable("id") Long id);
}
