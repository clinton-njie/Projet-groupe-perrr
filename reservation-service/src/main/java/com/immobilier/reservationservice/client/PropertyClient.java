package com.immobilier.reservationservice.client;

import com.immobilier.reservationservice.dto.external.PropertyDto;
import com.immobilier.reservationservice.dto.external.StatutBienUpdateRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "bien-service")
public interface PropertyClient {

    @GetMapping("/api/biens/{id}")
    PropertyDto getProperty(
            @PathVariable Long id
    );

    @PutMapping("/api/biens/{id}/statut")
    void changerStatut(
            @PathVariable Long id,
            @RequestBody StatutBienUpdateRequest request
    );
}
