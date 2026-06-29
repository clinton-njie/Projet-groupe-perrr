package com.immobilier.userservice.service;

import com.immobilier.userservice.entity.AuditLog;
import com.immobilier.userservice.entity.Utilisateur;
import com.immobilier.userservice.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Service d'audit pour enregistrer les actions des utilisateurs (création, modification, suppression, etc.)
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // Méthode pour enregistrer une action d'audit
    public void logAction(
            Utilisateur utilisateur,
            String action,
            String cibleType,
            Long cibleId,
            String details) {

        // Création d'une entrée d'audit avec les informations fournies
        AuditLog audit = AuditLog.builder()
                .utilisateur(utilisateur)
                .action(action)
                .cibleType(cibleType)
                .cibleId(cibleId)
                .details(details)
                .build();
        // Enregistrement de l'entrée d'audit dans la base de données
        auditLogRepository.save(audit);
    }
}