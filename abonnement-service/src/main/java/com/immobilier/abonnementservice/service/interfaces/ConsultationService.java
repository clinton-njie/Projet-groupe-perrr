package com.immobilier.abonnementservice.service.interfaces;

import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.dto.response.HistoriqueAbonnementResponse;
import com.immobilier.abonnementservice.dto.response.StatutAbonnementResponse;

import java.util.List;

public interface ConsultationService {

    // --- Agent (utilisateur connecté) ---
    StatutAbonnementResponse monStatut(String authHeader);
    HistoriqueAbonnementResponse monHistorique(String authHeader);
    AbonnementResponse monAbonnementActif(String authHeader);

    // --- Admin ---
    List<AbonnementResponse> tousLesAbonnements();
    AbonnementResponse parId(Long id);
    HistoriqueAbonnementResponse historiqueUtilisateur(Long userId);
    AbonnementResponse abonnementActifUtilisateur(Long userId);

    // --- Interne (autres microservices) ---
    StatutAbonnementResponse statutUtilisateur(Long userId);
    AbonnementResponse parRequestId(String requestId);
    long count();
}
