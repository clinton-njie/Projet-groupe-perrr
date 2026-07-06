package com.immobilier.abonnementservice.service.impl;

import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.dto.response.HistoriqueAbonnementResponse;
import com.immobilier.abonnementservice.dto.response.StatutAbonnementResponse;
import com.immobilier.abonnementservice.dto.response.UtilisateurConnecteResponse;
import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.exception.RessourceIntrouvableException;
import com.immobilier.abonnementservice.mapper.AbonnementMapper;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import com.immobilier.abonnementservice.security.JwtService;
import com.immobilier.abonnementservice.service.interfaces.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationServiceImpl implements ConsultationService {

    private final JwtService jwtService;
    private final AbonnementRepository abonnementRepository;

    // --- Agent ---

    @Override
    public StatutAbonnementResponse monStatut(String authHeader) {
        Long userId = jwtService.getUser(authHeader).getId();
        return statutUtilisateur(userId);
    }

    @Override
    public HistoriqueAbonnementResponse monHistorique(String authHeader) {
        Long userId = jwtService.getUser(authHeader).getId();
        return historiqueUtilisateur(userId);
    }

    @Override
    public AbonnementResponse monAbonnementActif(String authHeader) {
        Long userId = jwtService.getUser(authHeader).getId();
        return abonnementActifUtilisateur(userId);
    }

    // --- Admin ---

    @Override
    public List<AbonnementResponse> tousLesAbonnements() {
        return abonnementRepository.findAll().stream().map(AbonnementMapper::toResponse).toList();
    }

    @Override
    public AbonnementResponse parId(Long id) {
        return AbonnementMapper.toResponse(getOrThrow(id));
    }

    @Override
    public HistoriqueAbonnementResponse historiqueUtilisateur(Long userId) {
        List<AbonnementResponse> abonnements = abonnementRepository
                .findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(AbonnementMapper::toResponse).toList();
        return HistoriqueAbonnementResponse.builder()
                .userId(userId).nombre(abonnements.size()).abonnements(abonnements).build();
    }

    @Override
    public AbonnementResponse abonnementActifUtilisateur(Long userId) {
        Abonnement actif = actif(userId);
        return actif == null ? null : AbonnementMapper.toResponse(actif);
    }

    // --- Interne ---

    @Override
    public StatutAbonnementResponse statutUtilisateur(Long userId) {
        Abonnement actif = actif(userId);
        if (actif != null) {
            return AbonnementMapper.toStatut(actif);
        }
        // Pas d'abonnement actif : on renvoie le dernier connu (ou "aucun")
        return abonnementRepository.findTopByUserIdOrderByDateFinDesc(userId)
                .map(AbonnementMapper::toStatut)
                .orElse(StatutAbonnementResponse.builder().actif(false).build());
    }

    @Override
    public AbonnementResponse parRequestId(String requestId) {
        return abonnementRepository.findByRequestId(requestId)
                .map(AbonnementMapper::toResponse)
                .orElseThrow(() -> new RessourceIntrouvableException("Aucun abonnement pour requestId=" + requestId));
    }

    @Override
    public long count() {
        return abonnementRepository.count();
    }

    // --- helpers ---

    private Abonnement getOrThrow(Long id) {
        return abonnementRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Abonnement introuvable : " + id));
    }

    // Abonnement actif ET non expiré
    private Abonnement actif(Long userId) {
        return abonnementRepository
                .findFirstByUserIdAndStatutOrderByDateFinDesc(userId, StatutAbonnement.ACTIF)
                .filter(a -> a.getDateFin() != null && !a.getDateFin().isBefore(LocalDate.now()))
                .orElse(null);
    }
}
