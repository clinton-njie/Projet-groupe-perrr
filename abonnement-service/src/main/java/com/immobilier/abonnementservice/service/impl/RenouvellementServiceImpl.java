package com.immobilier.abonnementservice.service.impl;

import com.immobilier.abonnementservice.dto.request.RenouvelerAbonnementRequest;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.dto.response.UtilisateurConnecteResponse;
import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.mapper.AbonnementMapper;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import com.immobilier.abonnementservice.security.JwtService;
import com.immobilier.abonnementservice.service.interfaces.RenouvellementService;
import com.immobilier.abonnementservice.utils.PrixAbonnementUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class RenouvellementServiceImpl implements RenouvellementService {

    private final JwtService jwtService;
    private final AbonnementRepository abonnementRepository;

    @Override
    public AbonnementResponse renouveler(String token, RenouvelerAbonnementRequest request) {

        UtilisateurConnecteResponse user = jwtService.getUser(token);

        Abonnement last = abonnementRepository
                .findTopByAgentIdOrderByDateFinDesc(user.getId())
                .orElseThrow(() -> new RuntimeException("Aucun abonnement trouvé"));

        if (last.getStatut() == StatutAbonnement.ACTIF
                && last.getDateFin().isAfter(LocalDate.now())) {
            throw new RuntimeException("Abonnement encore actif");
        }

        LocalDate debut = LocalDate.now();

        last.setDateDebut(debut);
        last.setDateFin(PrixAbonnementUtils.calculerDateFin(debut, request.getType()));
        last.setType(request.getType());
        last.setPrix(PrixAbonnementUtils.calculerPrix(request.getType()));
        last.setStatut(StatutAbonnement.ACTIF);

        abonnementRepository.save(last);

        return AbonnementMapper.toResponse(last);
    }
}