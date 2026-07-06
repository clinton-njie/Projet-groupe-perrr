package com.plateforme.immo.avis.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Eureka normalise les noms de service en majuscules ; le vrai nom enregistré par
// service-user est "SERVICE-UTILISATEUR" (spring.application.name), pas "user-service".
@FeignClient(name = "service-utilisateur")
public interface UserServiceClient {

    // Le JWT ne porte que l'email (subject) + les rôles ; on résout l'ID interne ici.
    @GetMapping("/api/users/by-email/{email}")
    Long getAuthUserIdByEmail(@PathVariable("email") String email);
}
