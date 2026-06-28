package com.immobilier.abonnementservice.feign;

import com.immobilier.abonnementservice.dto.response.UtilisateurConnecteResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthFeignClient {

    @GetMapping("/api/interne/auth/utilisateur-connecte")
    UtilisateurConnecteResponse getUtilisateurConnecte(
            @RequestHeader("Authorization") String token
    );
}