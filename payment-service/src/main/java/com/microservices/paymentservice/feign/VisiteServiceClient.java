package com.microservices.paymentservice.feign;

import com.microservices.paymentservice.dto.VisitConfirmationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "service-visite")
public interface VisiteServiceClient {

@PutMapping("/visits/{id}/confirmation-paiement")
void confirmerPaiement(@PathVariable("id") Long id, @RequestBody VisitConfirmationRequest request);
}
