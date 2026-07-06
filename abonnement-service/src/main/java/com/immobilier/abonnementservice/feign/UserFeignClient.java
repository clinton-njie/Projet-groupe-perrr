package com.immobilier.abonnementservice.feign;

import com.immobilier.abonnementservice.feign.dto.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Source canonique de l'identité de l'agent : on ne fait jamais confiance au JWT seul.
// service-utilisateur (Eureka: SERVICE-UTILISATEUR) ne porte pas le rôle -> le rôle vient du JWT.
@FeignClient(name = "service-utilisateur")
public interface UserFeignClient {

    @GetMapping("/api/users/by-email/{email}")
    Long findIdByEmail(@PathVariable("email") String email);

    @GetMapping("/api/users/{id}")
    UserProfileDto findProfileById(@PathVariable("id") Long id);
}
