package com.projet_immo.service_signalements.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "service-reservation")
public interface ReservationClient {
    @GetMapping("api/reservations/exists")
    boolean existeInteraction(
        @RequestParam Long utilisateurId,
        @RequestParam Long agentId
    );
}
