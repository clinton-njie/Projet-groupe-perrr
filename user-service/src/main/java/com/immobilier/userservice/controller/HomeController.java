package com.immobilier.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Contrôleur REST pour la page d'accueil, 
// fournissant un endpoint de test pour vérifier que le service fonctionne correctement
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Le service de gestion utilisateurs marche correctement!";
    }
}