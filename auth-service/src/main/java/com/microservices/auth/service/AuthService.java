package com.microservices.auth.service;

import com.microservices.auth.dto.AuthResponse;
import com.microservices.auth.dto.LoginRequest;
import com.microservices.auth.dto.MessageResponse;
import com.microservices.auth.dto.RefreshTokenRequest;
import com.microservices.auth.dto.RegisterRequest;
import com.microservices.auth.entity.RefreshToken;
import com.microservices.auth.entity.Role;
import com.microservices.auth.entity.User;
import com.microservices.auth.enumtype.RoleName;
import com.microservices.auth.repository.RoleRepository;
import com.microservices.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public MessageResponse register(RegisterRequest request) {

        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Cet email est déjà utilisé");
        }

        Role clientRole = roleRepository.findByName(RoleName.ROLE_CLIENT)
                .orElseThrow(() -> new IllegalStateException(
                        "Le rôle ROLE_CLIENT est introuvable en base de données"
                ));

        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(roles)
                .build();

        userRepository.save(user);

        return MessageResponse.builder()
                .message("Utilisateur inscrit avec succès")
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().toLowerCase().trim();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Utilisateur introuvable"
                ));

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalStateException(
                        "Refresh token introuvable"
                ));

        RefreshToken verifiedRefreshToken = refreshTokenService.verifyRefreshToken(refreshToken);

        User user = verifiedRefreshToken.getUser();

        refreshTokenService.revokeRefreshToken(verifiedRefreshToken.getToken());

        String newAccessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .build();
    }

    @Transactional
    public MessageResponse logout(RefreshTokenRequest request) {

        refreshTokenService.revokeRefreshToken(request.getRefreshToken());

        return MessageResponse.builder()
                .message("Déconnexion réussie")
                .build();
    }
}