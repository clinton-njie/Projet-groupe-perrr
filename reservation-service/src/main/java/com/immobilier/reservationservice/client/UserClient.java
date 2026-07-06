package com.immobilier.reservationservice.client;

import com.immobilier.reservationservice.dto.external.UserDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Eureka normalise les noms de service en majuscules ; le vrai nom enregistré par
// service-user est "SERVICE-UTILISATEUR" (spring.application.name), pas "user-service".
@FeignClient(name = "service-utilisateur")
public interface UserClient {

    @GetMapping("/api/users/{authUserId}")
    UserDto getUserById(
            @PathVariable Long authUserId
    );

    // Le JWT émis par auth-service ne porte que le sujet (email) et les rôles,
    // pas d'id numérique -> il faut résoudre l'authUserId à partir de l'email.
    @GetMapping("/api/users/by-email/{email}")
    Long getAuthUserIdByEmail(
            @PathVariable String email
    );
}
