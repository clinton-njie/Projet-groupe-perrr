package com.immobilier.abonnementservice.service.impl;

import com.immobilier.abonnementservice.dto.response.*;
import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.mapper.AbonnementMapper;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import com.immobilier.abonnementservice.security.JwtService;
import com.immobilier.abonnementservice.service.interfaces.ConsultationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConsultationServiceImpl implements ConsultationService {

    private final JwtService jwtService;
    private final AbonnementRepository abonnementRepository;

    @Override
    public AbonnementResponse monAbonnement(String token) {

        Long userId = jwtService.getUserId(token);

        Abonnement abo = abonnementRepository
                .findTopByAgentIdOrderByDateFinDesc(userId)
                .orElseThrow(() -> new RuntimeException("Aucun abonnement"));

        return AbonnementMapper.toResponse(abo);
    }

    @Override
    public HistoriqueAbonnementResponse monHistorique(String token) {

        Long userId = jwtService.getUserId(token);

        List<Abonnement> list = abonnementRepository
                .findByAgentIdOrderByCreatedAtDesc(userId);

        return HistoriqueAbonnementResponse.builder()
                .agentId(userId)
                .abonnements(list.stream()
                        .map(AbonnementMapper::toResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public StatutAbonnementResponse monStatut(String token) {

        Long userId = jwtService.getUserId(token);

        Abonnement abo = abonnementRepository
                .findTopByAgentIdOrderByDateFinDesc(userId)
                .orElse(null);

        if (abo == null) {
            return StatutAbonnementResponse.builder()
                    .actif(false)
                    .build();
        }

        long jours = ChronoUnit.DAYS.between(LocalDate.now(), abo.getDateFin());

        return StatutAbonnementResponse.builder()
                .actif(abo.getStatut() == StatutAbonnement.ACTIF)
                .type(abo.getType())
                .statut(abo.getStatut())
                .dateDebut(abo.getDateDebut())
                .dateFin(abo.getDateFin())
                .joursRestants(jours)
                .build();
    }
}