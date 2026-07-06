package com.immobilier.abonnementservice.scheduler;

import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.kafka.EvenementHelper;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Expiration automatique + rappel "expiration proche" (notifications Kafka).
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpirationAbonnementScheduler {

    private static final int JOURS_AVANT_RAPPEL = 3;

    private final AbonnementRepository repository;
    private final EvenementHelper evenements;

    // Tous les jours à minuit : passe les actifs échus à EXPIRE + notifie
    @Scheduled(cron = "0 0 0 * * *")
    public void verifierExpirations() {
        List<Abonnement> echus = repository.findByDateFinBeforeAndStatut(LocalDate.now(), StatutAbonnement.ACTIF);
        for (Abonnement a : echus) {
            a.setStatut(StatutAbonnement.EXPIRE);
            repository.save(a);
            String requestId = a.getRequestId() != null ? a.getRequestId() : UUID.randomUUID().toString();
            evenements.notifier(requestId, a.getUserId(), a.getEmail(), "EXPIRE",
                    "Votre abonnement " + a.getType() + " a expiré. Pensez à le renouveler.");
            evenements.auditer(requestId, "ABONNEMENT_EXPIRE", a.getId(), a.getUserId(), "Expiration automatique");
            log.info("Abonnement {} expiré", a.getId());
        }
    }

    // Tous les jours à 8h : rappel pour les abonnements actifs expirant dans JOURS_AVANT_RAPPEL jours
    @Scheduled(cron = "0 0 8 * * *")
    public void rappelExpirationProche() {
        LocalDate cible = LocalDate.now().plusDays(JOURS_AVANT_RAPPEL);
        List<Abonnement> proches = repository.findByDateFinAndStatut(cible, StatutAbonnement.ACTIF);
        for (Abonnement a : proches) {
            String requestId = a.getRequestId() != null ? a.getRequestId() : UUID.randomUUID().toString();
            evenements.notifier(requestId, a.getUserId(), a.getEmail(), "EXPIRATION_PROCHE",
                    "Votre abonnement " + a.getType() + " expire le " + a.getDateFin() + ".");
        }
    }
}
