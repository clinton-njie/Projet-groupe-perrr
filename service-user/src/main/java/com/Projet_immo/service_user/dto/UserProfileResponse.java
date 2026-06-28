package com.Projet_immo.service_user.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private Long authUserId;
    private String firstname;
    private String lastname;
    private String email;
    private String telephone;
    private String adresse;
    private String ville;
    private String codePostal;
    private String pays;
    private String photoProfil;
    private String bio;
    private Boolean isActive;
    private Boolean isDeleted;
    private Instant deletedAt;
    private Instant scheduledDeletionAt;
    private Instant createdAt;
    private Instant updatedAt;
}
