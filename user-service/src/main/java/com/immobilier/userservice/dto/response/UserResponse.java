package com.immobilier.userservice.dto.response;

import com.immobilier.userservice.enums.Role;
import com.immobilier.userservice.enums.UserStatus;

import lombok.Data;

// DTO pour la réponse contenant les informations d'un utilisateur.
//  Contient l'id, email, prénom, nom, téléphone, rôle et statut actif de l'utilisateur.
@Data
public class UserResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Role role;
    private UserStatus status;
    private Boolean actif;
}