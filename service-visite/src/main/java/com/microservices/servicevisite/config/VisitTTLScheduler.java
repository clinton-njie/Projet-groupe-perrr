package com.microservices.servicevisite.config;

import com.microservices.servicevisite.entity.Visit;
import com.microservices.servicevisite.enums.VisitStatut;
import com.microservices.servicevisite.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class VisitTTLScheduler {

private final VisitRepository visitRepository;

@Scheduled(fixedRate = 60000) // toutes les 60 secondes
@Transactional
public void annulerVisitesExpirees() {
LocalDateTime limite = LocalDateTime.now().minusMinutes(15);

List<Visit> visitsExpirees = visitRepository
.findByStatutAndCreatedAtBefore(
VisitStatut.EN_ATTENTE_PAIEMENT,
limite
);

for (Visit visit : visitsExpirees) {
visit.setStatut(VisitStatut.ANNULEE);
visitRepository.save(visit);
log.info("Visite {} annulée après TTL de 15 minutes", visit.getId());
}
}
}

