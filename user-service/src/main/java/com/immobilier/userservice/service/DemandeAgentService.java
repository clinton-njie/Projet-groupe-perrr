package com.immobilier.userservice.service;

import com.immobilier.userservice.dto.request.DemandeAgentRequest;
import com.immobilier.userservice.entity.*;
import com.immobilier.userservice.enums.*;
import com.immobilier.userservice.exception.BusinessException;
import com.immobilier.userservice.repository.DemandeAgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Service pour gérer les demandes de devenir agent immobilier
@Service
@RequiredArgsConstructor
public class DemandeAgentService {

    private final DemandeAgentRepository repository;
    private final AuditService auditService;

    // Méthode pour créer une demande de devenir agent immobilier
    public DemandeAgent createDemande(
            Utilisateur utilisateur,
            DemandeAgentRequest request) {

        // Vérification que l'utilisateur est un client avant de pouvoir faire une demande d'agent
        if (utilisateur.getRole() != Role.CLIENT) {
            throw new BusinessException(
                    "Seul un CLIENT peut devenir AGENT");
        }

        // Vérification qu'il n'existe pas déjà une demande en attente pour cet utilisateur
        DemandeAgent demande =
                DemandeAgent.builder()
                        .utilisateur(utilisateur)
                        .motif(request.getMotif())
                        .build();

        demande = repository.save(demande);

        // Enregistrement de l'action d'audit pour la création de la demande d'agent
        auditService.logAction(
                utilisateur,
                "DEMANDE_AGENT",
                "DEMANDE",
                demande.getId(),
                "Création demande agent"
        );

        // Retour de la demande créée
        return demande;
    }
}