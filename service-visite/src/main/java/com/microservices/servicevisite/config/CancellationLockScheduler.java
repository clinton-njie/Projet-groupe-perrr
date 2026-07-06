package com.microservices.servicevisite.config;

import com.microservices.servicevisite.entity.Visit;
import com.microservices.servicevisite.enums.VisitStatut;
import com.microservices.servicevisite.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// Verrouille automatiquement l'annulation des visites qui démarrent dans moins de 24h,
// comme recommandé par la spec. Défense en profondeur : VisitService.annuler() fait aussi
// une vérification en temps réel pour combler l'écart entre deux passages de ce job.
@Component
@RequiredArgsConstructor
@Slf4j
public class CancellationLockScheduler {

private final VisitRepository visitRepository;

@Scheduled(fixedRate = 3600000) // toutes les heures
@Transactional
public void verrouillerAnnulationsProches() {
LocalDateTime limite = LocalDateTime.now().plusHours(24);

List<Visit> visitsAVerrouiller = visitRepository
.findByStatutInAndDateHeureDebutBeforeAndAnnulationVerrouilleeFalse(
List.of(VisitStatut.PAYEE, VisitStatut.EN_COURS),
limite
);

for (Visit visit : visitsAVerrouiller) {
visit.setAnnulationVerrouillee(true);
visitRepository.save(visit);
log.info("Annulation verrouillée pour la visite {} (moins de 24h avant le début)", visit.getId());
}
}
}
