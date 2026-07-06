package com.projet_immo.service_signalements.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "service-utilisateur")
public interface UtilisateurClient {
    @GetMapping("/api/users/{id}/email")
    String getEmailById(@PathVariable("id") Long id);

    // Le JWT ne porte que l'email (subject) ; on résout l'id numérique via user-service.
    @GetMapping("/api/users/by-email/{email}")
    Long getAuthUserIdByEmail(@PathVariable("email") String email);

    // Bannissement : désactivation du compte de l'agent sanctionné.
    @PutMapping("/internal/users/{authUserId}/deactivate")
    String deactivate(@PathVariable("authUserId") Long authUserId);
}