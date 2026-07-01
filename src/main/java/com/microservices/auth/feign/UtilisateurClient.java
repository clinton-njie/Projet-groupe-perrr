package com.microservices.auth.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.microservices.auth.dto.UtilisateurCreateRequest;

@FeignClient(name = "service-utilisateurs", url = "${services.utilisateurs.url}")
public interface UtilisateurClient {

    @PostMapping("/utilisateurs/creer-interne")
    void creerUtilisateur(@RequestBody UtilisateurCreateRequest request);
}
