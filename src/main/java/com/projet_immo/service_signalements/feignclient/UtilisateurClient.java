package com.projet_immo.service_signalements.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SERVICE-UTILISATEUR")
public interface UtilisateurClient {
    @GetMapping("/api/users/{id}/email")
    String getEmailById(@PathVariable("id") Long id);

}