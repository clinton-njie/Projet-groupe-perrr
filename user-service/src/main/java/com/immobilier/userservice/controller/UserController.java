package com.immobilier.userservice.controller;

import com.immobilier.userservice.dto.request.RegisterUserRequest;
import com.immobilier.userservice.dto.request.UpdateEmailRequest;
import com.immobilier.userservice.dto.request.UpdateNomRequest;
import com.immobilier.userservice.dto.request.UpdatePasswordRequest;
import com.immobilier.userservice.dto.request.UpdatePrenomRequest;
import com.immobilier.userservice.dto.request.UpdateTelephoneRequest;
import com.immobilier.userservice.dto.response.UserResponse;
import com.immobilier.userservice.entity.Utilisateur;
import com.immobilier.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Contrôleur REST pour la gestion des utilisateurs, exposant des endpoints
//  pour l'enregistrement et la consultation d'utilisateurs.
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Utilisateur register(
            @Valid @RequestBody RegisterUserRequest request) {

        return userService.register(request);
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PatchMapping("/{id}/nom")
    public UserResponse updateNom(
            @PathVariable Long id,
            @RequestBody UpdateNomRequest request) {

        return userService.updateNom(id, request);
    }

    @PatchMapping("/{id}/prenom")
    public UserResponse updatePrenom(
            @PathVariable Long id,
            @RequestBody UpdatePrenomRequest request) {

        return userService.updatePrenom(id, request);
    }

    @PatchMapping("/{id}/email")
    public UserResponse updateEmail(
            @PathVariable Long id,
            @RequestBody UpdateEmailRequest request) {

        return userService.updateEmail(id, request);
    }

    @PatchMapping("/{id}/telephone")
    public UserResponse updateTelephone(
            @PathVariable Long id,
            @RequestBody UpdateTelephoneRequest request) {

        return userService.updateTelephone(id, request);
    }

    @PatchMapping("/{id}/password")
    public UserResponse updatePassword(
            @PathVariable Long id,
            @RequestBody UpdatePasswordRequest request) {

        return userService.updatePassword(id, request);
    }
}
