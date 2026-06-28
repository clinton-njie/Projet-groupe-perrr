package com.immobilier.abonnementservice.feign;

import com.immobilier.abonnementservice.dto.request.PaiementRequest;
import com.immobilier.abonnementservice.dto.response.PaiementResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAIEMENT-SERVICE")
public interface PaiementFeignClient {

    @PostMapping("/api/paiements")
    PaiementResponse creerPaiement(
            @RequestBody PaiementRequest request
    );
}