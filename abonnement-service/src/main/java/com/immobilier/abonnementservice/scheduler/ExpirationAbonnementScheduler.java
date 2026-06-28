package com.immobilier.abonnementservice.scheduler;

import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpirationAbonnementScheduler {

    private final AbonnementRepository repository;
    //private final AbonnementProducer producer;

    @Scheduled(cron = "0 0 0 * * *")
    public void verifierExpirations() {

        List<Abonnement> abonnements = repository
                .findByDateFinBeforeAndStatut(
                        LocalDate.now(),
                        StatutAbonnement.ACTIF
                );

        for (Abonnement abonnement : abonnements) {

            abonnement.setStatut(StatutAbonnement.EXPIRE);

            repository.save(abonnement);

           /* producer.sendAudit(
                    "ABONNEMENT_EXPIRE",
                    "ABONNEMENT-SERVICE",
                    "Abonnement " + abonnement.getId() + " expiré"
            );

            log.info("Abonnement {} expiré", abonnement.getId()); */
        }
    }

}