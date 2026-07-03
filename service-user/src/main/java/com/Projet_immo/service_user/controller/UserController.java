package com.Projet_immo.service_user.controller;

import com.Projet_immo.service_user.dto.*;
import com.Projet_immo.service_user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Initialiser profil (appelé par auth-service)
    @PostMapping("/init")
    public ResponseEntity<UserProfileResponse> initProfile(@Valid @RequestBody UserInitRequest request) {
        return ResponseEntity.ok(userService.initProfile(request));
    }

    // Récupérer un profil
    @GetMapping("/{authUserId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long authUserId) {
        return ResponseEntity.ok(userService.getProfile(authUserId));
    }
    // Récupérer authUserId à partir de l'email (appelé par les autres microservices)
    @GetMapping("/by-email/{email}")
    public ResponseEntity<Long> getAuthUserIdByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getAuthUserIdByEmail(email));
    }
    // Récupérer l'email à partir de l'authUserId (appelé par d'autres microservices)
    @GetMapping("/{authUserId}/email")
    public ResponseEntity<String> getEmailByAuthUserId(@PathVariable Long authUserId) {
        return ResponseEntity.ok(userService.getEmailByAuthUserId(authUserId));
    }
    // Modifier un profil
    @PutMapping("/{authUserId}")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long authUserId,
            @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(authUserId, request));
    }

    // Soft delete
    @DeleteMapping("/{authUserId}")
    public ResponseEntity<String> deleteProfile(@PathVariable Long authUserId) {
        return ResponseEntity.ok(userService.deleteProfile(authUserId));
    }

    // Restaurer un compte
    @PostMapping("/{authUserId}/restore")
    public ResponseEntity<String> restoreProfile(@PathVariable Long authUserId) {
        return ResponseEntity.ok(userService.restoreProfile(authUserId));
    }

    // Statut de suppression
    @GetMapping("/{authUserId}/deletion-status")
    public ResponseEntity<String> getDeletionStatus(@PathVariable Long authUserId) {
        return ResponseEntity.ok(userService.getDeletionStatus(authUserId));
    }

    // Demander changement de rôle (la gestion de la demande vit dans service-admin)
    @PostMapping("/{authUserId}/request-role-change")
    public ResponseEntity<String> requestRoleChange(
            @PathVariable Long authUserId,
            @Valid @RequestBody RoleChangeRequestDto request) {
        return ResponseEntity.ok(userService.requestRoleChange(authUserId, request));
    }
}
