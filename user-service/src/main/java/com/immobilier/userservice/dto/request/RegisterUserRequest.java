package com.immobilier.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

//DTO(Data Transfer Object) pour l'inscription d'un utilisateur. Contient les champs nécessaires pour créer un compte.
@Data
public class RegisterUserRequest {

    @NotBlank
    private String nom;
    
    @NotBlank
    private String prenom;

    @NotBlank // Email obligatoire
    @Email // Format email valide
    private String email;

    @NotBlank
    private String telephone;

    @NotBlank // Mot de passe obligatoire
    private String motDePasse;


}