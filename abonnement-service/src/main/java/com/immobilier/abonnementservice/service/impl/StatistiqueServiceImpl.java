package com.immobilier.abonnementservice.service.impl;

import com.immobilier.abonnementservice.dto.response.StatistiqueAbonnementResponse;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.enums.TypeAbonnement;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import com.immobilier.abonnementservice.service.interfaces.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatistiqueServiceImpl implements StatistiqueService {

    private final AbonnementRepository repo;

    @Override
    public StatistiqueAbonnementResponse statistiquesGlobales() {
        return StatistiqueAbonnementResponse.builder()
                .totalAbonnements(repo.count())
                .abonnementsActifs(repo.countByStatut(StatutAbonnement.ACTIF))
                .abonnementsExpires(repo.countByStatut(StatutAbonnement.EXPIRE))
                .abonnementsAnnules(repo.countByStatut(StatutAbonnement.ANNULE))
                .abonnementsEnAttente(repo.countByStatut(StatutAbonnement.EN_ATTENTE_PAIEMENT))
                .abonnementsEchecsPaiement(repo.countByStatut(StatutAbonnement.ECHEC_PAIEMENT))
                .abonnementsMensuels(repo.countByType(TypeAbonnement.MENSUEL))
                .abonnementsAnnuels(repo.countByType(TypeAbonnement.ANNUEL))
                .build();
    }

    @Override
    public StatistiqueAbonnementResponse statistiquesParPeriode(LocalDate debut, LocalDate fin) {
        long total = repo.countByCreatedAtBetween(debut.atStartOfDay(), fin.atTime(LocalTime.MAX));
        return StatistiqueAbonnementResponse.builder()
                .debutPeriode(debut)
                .finPeriode(fin)
                .totalSurPeriode(total)
                .totalAbonnements(repo.count())
                .abonnementsActifs(repo.countByStatut(StatutAbonnement.ACTIF))
                .abonnementsExpires(repo.countByStatut(StatutAbonnement.EXPIRE))
                .abonnementsAnnules(repo.countByStatut(StatutAbonnement.ANNULE))
                .abonnementsEnAttente(repo.countByStatut(StatutAbonnement.EN_ATTENTE_PAIEMENT))
                .abonnementsEchecsPaiement(repo.countByStatut(StatutAbonnement.ECHEC_PAIEMENT))
                .abonnementsMensuels(repo.countByType(TypeAbonnement.MENSUEL))
                .abonnementsAnnuels(repo.countByType(TypeAbonnement.ANNUEL))
                .build();
    }
}
