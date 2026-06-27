package com.immobilier.reservationservice.client;

import com.immobilier.reservationservice.dto.external.PropertyDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "BIEN-SERVICE") // Déclaration d'un client Feign pour interagir avec le service de gestion des biens
public interface PropertyClient {

    @GetMapping("/api/biens/{id}")
    PropertyDto getProperty(
            @PathVariable Long id
    );
}