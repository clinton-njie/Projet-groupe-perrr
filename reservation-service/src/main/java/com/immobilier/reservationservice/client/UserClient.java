package com.immobilier.reservationservice.client;

import com.immobilier.reservationservice.dto.external.UserDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE") // Nom du service utilisateur pour la communication via Feign
public interface UserClient {
    //Classe représentant le client Feign pour communiquer avec le service utilisateur

    @GetMapping("/api/users/{id}") 
    UserDto getUserById(
            @PathVariable Long id
    ); 
}