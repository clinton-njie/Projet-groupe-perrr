package com.Projet_immo.service_user.service;

import com.Projet_immo.service_user.dto.*;
import com.Projet_immo.service_user.entity.RoleChangeRequest;
import com.Projet_immo.service_user.entity.UserProfile;
import com.Projet_immo.service_user.repository.RoleChangeRequestRepository;
import com.Projet_immo.service_user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository userProfileRepository;
    private final RoleChangeRequestRepository roleChangeRequestRepository;
    private final RestTemplate restTemplate;

    @Value("${services.auth-service-url}")
    private String authServiceUrl;

    // Créer profil après inscription (appelé par auth-service)
    public UserProfileResponse initProfile(UserInitRequest request) {
        UserProfile profile = UserProfile.builder()
                .authUserId(request.getAuthUserId())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .build();
        userProfileRepository.save(profile);
        return mapToResponse(profile);
    }

    // Récupérer un profil
    public UserProfileResponse getProfile(Long authUserId) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
        return mapToResponse(profile);
    }

    // Récupérer authUserId à partir de l'email (usage interne inter-services)
    public Long getAuthUserIdByEmail(String email) {
        UserProfile profile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profil introuvable pour cet email"));
        return profile.getAuthUserId();
    }
    // Récupérer l'email à partir de l'authUserId (pour service-signalements et autres)
    public String getEmailByAuthUserId(Long authUserId) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return profile.getEmail();
    }

    // Modifier un profil
    public UserProfileResponse updateProfile(Long authUserId, UserProfileRequest request) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
        if (request.getTelephone() != null) profile.setTelephone(request.getTelephone());
        if (request.getAdresse() != null) profile.setAdresse(request.getAdresse());
        if (request.getVille() != null) profile.setVille(request.getVille());
        if (request.getCodePostal() != null) profile.setCodePostal(request.getCodePostal());
        if (request.getPays() != null) profile.setPays(request.getPays());
        if (request.getPhotoProfil() != null) profile.setPhotoProfil(request.getPhotoProfil());
        if (request.getBio() != null) profile.setBio(request.getBio());
        profile.setUpdatedAt(Instant.now());
        userProfileRepository.save(profile);
        return mapToResponse(profile);
    }

    // Soft delete (par l'utilisateur)
    public String deleteProfile(Long authUserId) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
        profile.setIsDeleted(true);
        profile.setIsActive(false);
        profile.setDeletedAt(Instant.now());
        profile.setScheduledDeletionAt(Instant.now().plus(30, ChronoUnit.DAYS));
        userProfileRepository.save(profile);
        return "Compte supprimé. Vous avez 30 jours pour le restaurer.";
    }

    // Restaurer un compte
    public String restoreProfile(Long authUserId) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
        if (!profile.getIsDeleted()) {
            return "Ce compte n'est pas supprimé.";
        }
        if (Instant.now().isAfter(profile.getScheduledDeletionAt())) {
            return "Le délai de restauration est expiré.";
        }
        profile.setIsDeleted(false);
        profile.setIsActive(true);
        profile.setDeletedAt(null);
        profile.setScheduledDeletionAt(null);
        userProfileRepository.save(profile);
        return "Compte restauré avec succès.";
    }

    // Statut de suppression
    public String getDeletionStatus(Long authUserId) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
        if (!profile.getIsDeleted()) return "Ce compte n'est pas en cours de suppression.";
        long joursRestants = ChronoUnit.DAYS.between(Instant.now(), profile.getScheduledDeletionAt());
        return "Suppression définitive dans " + joursRestants + " jour(s).";
    }

    // Désactiver un compte (par admin)
    public String deactivateProfile(Long authUserId) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
        profile.setIsActive(false);
        userProfileRepository.save(profile);
        return "Compte désactivé avec succès.";
    }

    // Lister tous les profils (admin)
    public List<UserProfileResponse> getAllProfiles() {
        return userProfileRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Demander changement de rôle
    public String requestRoleChange(Long authUserId, RoleChangeRequestDto request) {
        UserProfile profile = userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new RuntimeException("Profil introuvable"));
        RoleChangeRequest roleRequest = RoleChangeRequest.builder()
                .authUserId(authUserId)
                .currentRole("ROLE_CLIENT")
                .requestedRole(request.getRequestedRole())
                .justification(request.getJustification())
                .build();
        roleChangeRequestRepository.save(roleRequest);
        return "Demande de changement de rôle envoyée avec succès.";
    }

    // Lister toutes les demandes (admin)
    public List<RoleChangeRequest> getAllRoleRequests() {
        return roleChangeRequestRepository.findAll();
    }

    // Traiter une demande (admin)
    public String processRoleRequest(Long requestId, ProcessRoleRequestDto request) {
        RoleChangeRequest roleRequest = roleChangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        if ("APPROUVÉ".equals(request.getDecision())) {
            roleRequest.setStatus("APPROUVÉ");
            roleRequest.setProcessedAt(Instant.now());

            // Appel à l'auth-service pour appliquer le nouveau rôle
            RoleUpdateRequest roleUpdateRequest = RoleUpdateRequest.builder()
                    .newRole(roleRequest.getRequestedRole())
                    .build();

            String url = authServiceUrl + "/api/auth/users/" + roleRequest.getAuthUserId() + "/role";
            restTemplate.put(url, roleUpdateRequest);

        } else {
            roleRequest.setStatus("REFUSÉ");
            roleRequest.setRefusalReason(request.getRefusalReason());
            roleRequest.setProcessedAt(Instant.now());
        }
        roleChangeRequestRepository.save(roleRequest);
        return "Demande traitée avec succès.";
    }

    // Suppression automatique après 30 jours
    @Scheduled(cron = "0 0 2 * * *")
    public void purgeDeletedAccounts() {
        List<UserProfile> toDelete = userProfileRepository
                .findByIsDeletedTrueAndScheduledDeletionAtBefore(Instant.now());
        userProfileRepository.deleteAll(toDelete);
    }

    // Mapper entité vers DTO
    private UserProfileResponse mapToResponse(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .authUserId(profile.getAuthUserId())
                .firstname(profile.getFirstname())
                .lastname(profile.getLastname())
                .email(profile.getEmail())
                .telephone(profile.getTelephone())
                .adresse(profile.getAdresse())
                .ville(profile.getVille())
                .codePostal(profile.getCodePostal())
                .pays(profile.getPays())
                .photoProfil(profile.getPhotoProfil())
                .bio(profile.getBio())
                .isActive(profile.getIsActive())
                .isDeleted(profile.getIsDeleted())
                .deletedAt(profile.getDeletedAt())
                .scheduledDeletionAt(profile.getScheduledDeletionAt())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
