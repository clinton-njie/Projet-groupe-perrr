package com.microservices.auth.controller;

import com.microservices.auth.dto.AuthResponse;
import com.microservices.auth.dto.CurrentUserResponse;
import com.microservices.auth.dto.LoginRequest;
import com.microservices.auth.dto.MessageResponse;
import com.microservices.auth.dto.RefreshTokenRequest;
import com.microservices.auth.dto.RegisterAdminRequest;
import com.microservices.auth.dto.RegisterRequest;
import com.microservices.auth.dto.RoleUpdateRequest;
import com.microservices.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {

        MessageResponse response = authService.register(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {

        MessageResponse response = authService.logout(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(Authentication authentication) {

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        CurrentUserResponse response = CurrentUserResponse.builder()
                .email(authentication.getName())
                .roles(roles)
                .build();

        return ResponseEntity.ok(response);
    }

    // Appelé par service-admin quand une demande de changement de rôle est approuvée
    @PutMapping("/users/{id}/role")
    public ResponseEntity<MessageResponse> updateRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest request) {

        return ResponseEntity.ok(authService.updateRole(id, request));
    }

    // Appelé par service-admin quand une invitation admin est acceptée
    @PostMapping("/admin/register")
    public ResponseEntity<MessageResponse> registerAdmin(@Valid @RequestBody RegisterAdminRequest request) {

        return ResponseEntity.ok(authService.registerAdmin(request));
    }
}