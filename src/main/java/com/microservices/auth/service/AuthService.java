package com.microservices.auth.service;

import com.microservices.auth.dto.AuthResponse;
import com.microservices.auth.dto.LoginRequest;
import com.microservices.auth.dto.MessageResponse;
import com.microservices.auth.dto.RefreshTokenRequest;
import com.microservices.auth.dto.RegisterRequest;
import com.microservices.auth.dto.RegisterResponse;
import com.microservices.auth.dto.UtilisateurCreateRequest;
import com.microservices.auth.entity.RefreshToken;
import com.microservices.auth.entity.Role;
import com.microservices.auth.entity.User;
import com.microservices.auth.enumtype.RoleName;
import com.microservices.auth.feign.UtilisateurClient;
import com.microservices.auth.repository.RoleRepository;
import com.microservices.auth.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UtilisateurClient utilisateurClient;  // ← nouveau

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       UtilisateurClient utilisateurClient) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.utilisateurClient = utilisateurClient;
    }

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

        // On ne met plus firstname/lastname ici — l'entité User n'a plus ces champs
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRoles(roles);

        // Étape 1 : sauvegarde dans auth → génère l'id
        User savedUser = userRepository.save(user);

        // Étape 2 : envoyer firstname, lastname et l'id à service-utilisateurs
        UtilisateurCreateRequest utilisateurRequest = new UtilisateurCreateRequest(
                savedUser.getId(),
                request.getFirstname(),
                request.getLastname(),
                email
        );

        try {
            utilisateurClient.creerUtilisateur(utilisateurRequest);
        } catch (Exception e) {
            // Compensation : si service-utilisateurs échoue, on annule la création dans auth
            userRepository.delete(savedUser);
            throw new IllegalStateException(
                    "Échec de la création du profil utilisateur : " + e.getMessage()
            );
        }

        // Étape 3 : construire la réponse avec firstname/lastname (depuis le request, pas l'entité)
        Set<String> roleNames = savedUser.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new RegisterResponse(
                "Utilisateur inscrit avec succès",
                request.getFirstname(),   // ← vient du request, pas de l'entité
                request.getLastname(),
                email,
                roleNames
        );
    }

    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().toLowerCase().trim();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));

        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        // firstname/lastname ne viennent plus de l'entité User
        // → soit on les récupère depuis service-utilisateurs via Feign,
        //   soit on les retire de AuthResponse (recommandé pour rester simple)
        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                null,   // firstname — voir note ci-dessous
                null,   // lastname
                user.getEmail(),
                roleNames
        );
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalStateException("Refresh token introuvable"));

        RefreshToken verifiedRefreshToken = refreshTokenService.verifyRefreshToken(refreshToken);
        User user = verifiedRefreshToken.getUser();

        refreshTokenService.revokeRefreshToken(verifiedRefreshToken.getToken());

        String newAccessToken = jwtService.generateAccessToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                "Bearer",
                null,   // firstname
                null,   // lastname
                user.getEmail(),
                roleNames
        );
    }

    @Transactional
    public MessageResponse logout(RefreshTokenRequest request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        return new MessageResponse("Déconnexion réussie");
    }
}