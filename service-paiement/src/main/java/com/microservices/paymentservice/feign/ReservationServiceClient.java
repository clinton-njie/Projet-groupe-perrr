package com.microservices.paymentservice.feign;

import com.microservices.paymentservice.dto.ReservationCallbackRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "reservation-service")
public interface ReservationServiceClient {

    @PutMapping("/api/reservations/{id}/confirmer")
    void confirmer(@PathVariable("id") Long id, @RequestBody ReservationCallbackRequest request);

    @PutMapping("/api/reservations/{id}/annuler")
    void annuler(@PathVariable("id") Long id, @RequestBody ReservationCallbackRequest request);
}
