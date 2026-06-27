package com.microservices.servicevisite.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

@PostMapping("/payments")
PaymentResponse initierPaiement(@RequestBody PaymentRequest request);
}
