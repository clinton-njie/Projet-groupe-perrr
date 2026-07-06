package com.microservices.auth.service;

import com.microservices.auth.dto.AuthResponse;
import com.microservices.auth.dto.LoginRequest;
import com.microservices.auth.dto.LoginResponse;
import com.microservices.auth.dto.MessageResponse;
import com.microservices.auth.dto.RefreshTokenRequest;
import com.microservices.auth.dto.RegisterAdminRequest;
import com.microservices.auth.dto.RegisterRequest;
import com.microservices.auth.dto.RegisterResponse;
import com.microservices.auth.dto.RoleUpdateRequest;
import com.microservices.auth.dto.UserInitRequest;
import com.microservices.auth.entity.RefreshToken;
import com.microservices.auth.entity.Role;
import com.microservices.auth.entity.User;
import com.microservices.auth.enumtype.RoleName;
import com.microservices.auth.repository.RoleRepository;
import com.microservices.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;

    @Value("${services.user-service-url}")
    private String userServiceUrl;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

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
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(roles)
                .build();

        userRepository.save(user);

        notifyUserService(user.getId(), request.getFirstname(), request.getLastname(), email);

        return RegisterResponse.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(email)
                .role(clientRole.getName().name())
                .build();
    }

    public LoginResponse login(LoginRequest request) {

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

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .role(resolveRoleName(user.getRoles()))
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

    @Transactional
    public MessageResponse updateRole(Long userId, RoleUpdateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));

        RoleName roleName = RoleName.valueOf(request.getNewRole());

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException(
                        "Le rôle " + roleName + " est introuvable en base de données"
                ));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);

        return MessageResponse.builder()
                .message("Rôle mis à jour avec succès")
                .build();
    }

    // Appelé par service-admin lors de l'acceptation d'une invitation admin.
    @Transactional
    public MessageResponse registerAdmin(RegisterAdminRequest request) {

        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Cet email est déjà utilisé");
        }

        RoleName roleName = RoleName.valueOf(request.getRole());

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException(
                        "Le rôle " + roleName + " est introuvable en base de données"
                ));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .roles(roles)
                .build();

        userRepository.save(user);

        notifyUserService(user.getId(), request.getFirstname(), request.getLastname(), email);

        return MessageResponse.builder()
                .message("Compte administrateur créé avec succès")
                .build();
    }

    // Chaque utilisateur ne porte qu'un seul rôle métier à la fois (voir updateRole/registerAdmin).
    private String resolveRoleName(Set<Role> roles) {
        return roles.stream()
                .findFirst()
                .map(role -> role.getName().name())
                .orElse(null);
    }

    // Transmet le prénom/nom saisis à l'inscription à service-user,
    // qui reste seul propriétaire de ces données de profil.
    private void notifyUserService(Long authUserId, String firstname, String lastname, String email) {

        UserInitRequest initRequest = UserInitRequest.builder()
                .authUserId(authUserId)
                .firstname(firstname)
                .lastname(lastname)
                .email(email)
                .build();

        restTemplate.postForObject(userServiceUrl + "/api/users/init", initRequest, Object.class);
    }
}