package com.Projet_immo.service_user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileRequest {
    private String telephone;
    private String adresse;
    private String ville;
    private String codePostal;
    private String pays;
    private String photoProfil;
    private String bio;
}
