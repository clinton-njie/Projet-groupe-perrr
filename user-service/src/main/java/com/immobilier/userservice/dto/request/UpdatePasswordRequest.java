package com.immobilier.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


// DTO pour la mise à jour du mot de passe d'un utilisateur.
public class UpdatePasswordRequest {

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6)
    private String password;

    // Getter et Setter pour le mot de passe
    //Necessaire pour la sérialisation et la désérialisation des données lors de la communication avec le client.
    //si lombok n'est pas utilisé.   
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}