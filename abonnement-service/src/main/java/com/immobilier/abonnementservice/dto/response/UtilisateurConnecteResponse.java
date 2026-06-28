package com.immobilier.abonnementservice.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UtilisateurConnecteResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private boolean actif;

}