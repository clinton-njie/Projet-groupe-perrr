package com.immobilier.abonnementservice.feign.dto;

import lombok.Getter;
import lombok.Setter;

// Projection du profil renvoyé par service-utilisateur (GET /api/users/{id}).
@Getter
@Setter
public class UserProfileDto {
    private Long authUserId;
    private String firstname;
    private String lastname;
    private String email;
    private Boolean isActive;
}
