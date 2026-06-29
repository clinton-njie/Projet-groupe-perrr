package com.immobilier.userservice.service;

import com.immobilier.userservice.dto.request.RegisterUserRequest;
import com.immobilier.userservice.dto.request.UpdateEmailRequest;
import com.immobilier.userservice.dto.request.UpdateNomRequest;
import com.immobilier.userservice.dto.request.UpdatePasswordRequest;
import com.immobilier.userservice.dto.request.UpdatePrenomRequest;
import com.immobilier.userservice.dto.request.UpdateRoleRequest;
import com.immobilier.userservice.dto.request.UpdateStatusRequest;
import com.immobilier.userservice.dto.request.UpdateTelephoneRequest;
import com.immobilier.userservice.dto.response.UserResponse;
import com.immobilier.userservice.enums.Role;

import java.util.List;
import com.immobilier.userservice.entity.Utilisateur;
import com.immobilier.userservice.exception.BusinessException;
import com.immobilier.userservice.exception.ResourceNotFoundException;
import com.immobilier.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Service principal pour la gestion des utilisateurs (création, recherche, etc.)
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    // Méthode pour enregistrer un nouvel utilisateur
    public Utilisateur register(RegisterUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email déjà utilisé");
        }

        // Conversion de la requête en entité utilisateur
        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setEmail(request.getEmail());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setNom(request.getNom());
        utilisateur.setRole(Role.CLIENT);

        // Encodage du mot de passe avant de le stocker
        utilisateur.setMotDePasse(
                passwordEncoder.encode(
                        request.getMotDePasse()
                )
        );

        utilisateur =
                userRepository.save(utilisateur);

        // Enregistrement de l'action d'audit pour la création du compte
        auditService.logAction(
                utilisateur,
                "REGISTER",
                "UTILISATEUR",
                utilisateur.getId(),
                "Création du compte"
        );

        return utilisateur;
    }

    // Méthode pour trouver un utilisateur par son ID
    public UserResponse findById(@NonNull Long id) {

        // Recherche de l'utilisateur dans la base de données, avec gestion de l'exception si non trouvé
        Utilisateur utilisateur =
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Utilisateur introuvable"));

        UserResponse response = new UserResponse();

        response.setId(utilisateur.getId());
        response.setEmail(utilisateur.getEmail());
        response.setPrenom(utilisateur.getPrenom());
        response.setNom(utilisateur.getNom());

        return response;
    }

    public List<UserResponse> findAll() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse updateNom(Long id,
                                  UpdateNomRequest request) {

        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable"));

        user.setNom(request.getNom());

        return mapToResponse(
                userRepository.save(user)
        );
    }

    public UserResponse updatePrenom(Long id,
                                     UpdatePrenomRequest request) {

        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable"));

        user.setPrenom(request.getPrenom());

        return mapToResponse(
                userRepository.save(user)
        );
    }

    public UserResponse updateEmail(Long id,
                                    UpdateEmailRequest request) {

        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable"));

        user.setEmail(request.getEmail());

        return mapToResponse(
                userRepository.save(user)
        );
    }

    // Méthode pour mettre à jour le numéro de téléphone d'un utilisateur
    public UserResponse updateTelephone(
        Long id,
        UpdateTelephoneRequest request) {

        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Utilisateur introuvable"));

        user.setTelephone(request.getTelephone());

        user = userRepository.save(user);

        auditService.logAction(
                user,
                "UPDATE_TELEPHONE",
                "UTILISATEUR",
                user.getId(),
                "Modification du téléphone"
        );

        return mapToResponse(user);
    }

    // Méthode pour mettre à jour le mot de passe d'un utilisateur
    public UserResponse updatePassword(
            Long id,
            UpdatePasswordRequest request) {

        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Utilisateur introuvable"));

        user.setMotDePasse(
                passwordEncoder.encode(
                        request.getPassword())
        );

        user = userRepository.save(user);

        auditService.logAction(
                user,
                "UPDATE_PASSWORD",
                "UTILISATEUR",
                user.getId(),
                "Modification du mot de passe"
        );

        return mapToResponse(user);
    }

    // Méthode pour enregistrer un nouvel administrateur
    public UserResponse registerAdmin(RegisterUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email déjà utilisé");
        }

        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(request.getTelephone());

        utilisateur.setRole(Role.ADMIN);

        utilisateur.setMotDePasse(
                passwordEncoder.encode(
                        request.getMotDePasse()
                )
        );

        utilisateur = userRepository.save(utilisateur);

        auditService.logAction(
                utilisateur,
                "REGISTER_ADMIN",
                "UTILISATEUR",
                utilisateur.getId(),
                "Création d'un administrateur"
        );

        return mapToResponse(utilisateur);
    }

    // Méthode pour mettre à jour le rôle d'un utilisateur
    public UserResponse updateRole(
        Long id,
        UpdateRoleRequest request) {

        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Utilisateur introuvable"));

        user.setRole(request.getRole());

        user = userRepository.save(user);

        auditService.logAction(
                user,
                "UPDATE_ROLE",
                "UTILISATEUR",
                user.getId(),
                "Modification du rôle vers "
                        + request.getRole()
        );

        return mapToResponse(user);
    }

    // Méthode pour mettre à jour le status d'un utilisateur
    public UserResponse updateStatus(
        Long id,
        UpdateStatusRequest request) {

        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Utilisateur introuvable"));

        user.setStatus(request.getStatus());

        user = userRepository.save(user);

        auditService.logAction(
                user,
                "UPDATE_STATUS",
                "UTILISATEUR",
                user.getId(),
                "Modification du status vers "
                        + request.getStatus()
        );

        return mapToResponse(user);
    }
    
    public void delete(Long id) {

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur introuvable");
        }

        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(Utilisateur user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setNom(user.getNom());
        response.setPrenom(user.getPrenom());
        response.setEmail(user.getEmail());

        return response;
    }
}