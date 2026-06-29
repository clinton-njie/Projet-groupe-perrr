package com.immobilier.userservice.controller;

import com.immobilier.userservice.dto.request.RegisterUserRequest;
import com.immobilier.userservice.dto.request.UpdateRoleRequest;
import com.immobilier.userservice.dto.request.UpdateStatusRequest;
import com.immobilier.userservice.dto.response.UserResponse;
import com.immobilier.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Contrôleur REST pour la gestion des fonctionnalités administratives, actuellement sans endpoints définis
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController<userService> {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerAdmin(
            @Valid @RequestBody RegisterUserRequest request) {

        return userService.registerAdmin(request);
    }

    @GetMapping("/users")
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @PatchMapping("/users/{id}/role")
    public UserResponse updateRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request) {

        return userService.updateRole(id, request);
    }

    @PatchMapping("/users/{id}/status")
    public UserResponse updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {

        return userService.updateStatus(id, request);
    }

    // Endpoint pour supprimer un utilisateur par son ID, 
    // accessible uniquement aux administrateurs
    @DeleteMapping("/users/{id}/delete")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}