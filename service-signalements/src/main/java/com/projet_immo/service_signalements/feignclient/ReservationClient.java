package com.projet_immo.service_signalements.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "reservation-service")
public interface ReservationClient {
    @GetMapping("/api/reservations/verification")
    boolean existeInteraction(
        @RequestParam Long utilisateurId,
        @RequestParam Long agentId
    );
} 
