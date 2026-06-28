package com.immobilier.abonnementservice.service.impl;

import com.immobilier.abonnementservice.dto.response.StatistiqueAbonnementResponse;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.enums.TypeAbonnement;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import com.immobilier.abonnementservice.service.interfaces.StatistiqueService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatistiqueServiceImpl implements StatistiqueService {

    private final AbonnementRepository abonnementRepository;

    @Override
    public StatistiqueAbonnementResponse statistiques() {

        return StatistiqueAbonnementResponse.builder()
                .totalAbonnements(abonnementRepository.count())
                .abonnementsActifs(abonnementRepository.countByStatut(StatutAbonnement.ACTIF))
                .abonnementsAnnules(abonnementRepository.countByStatut(StatutAbonnement.ANNULE))
                .abonnementsExpires(abonnementRepository.countByStatut(StatutAbonnement.EXPIRE))
                .abonnementsMensuels(abonnementRepository.countByType(TypeAbonnement.MENSUEL))
                .abonnementsAnnuels(abonnementRepository.countByType(TypeAbonnement.ANNUEL))
                .build();
    }
}