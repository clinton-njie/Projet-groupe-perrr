package com.Projet_immo.service_user.controller;

import com.Projet_immo.service_user.dto.UserProfileResponse;
import com.Projet_immo.service_user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints appelés uniquement par service-admin (pas par le frontend directement) :
// service-user reste propriétaire des données UserProfile, service-admin orchestre.
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> getAllProfiles() {
        return ResponseEntity.ok(userService.getAllProfiles());
    }

    @PutMapping("/{authUserId}/deactivate")
    public ResponseEntity<String> deactivateProfile(@PathVariable Long authUserId) {
        return ResponseEntity.ok(userService.deactivateProfile(authUserId));
    }

    @PutMapping("/{authUserId}/reactivate")
    public ResponseEntity<String> reactivateProfile(@PathVariable Long authUserId) {
        return ResponseEntity.ok(userService.reactivateProfile(authUserId));
    }
}
