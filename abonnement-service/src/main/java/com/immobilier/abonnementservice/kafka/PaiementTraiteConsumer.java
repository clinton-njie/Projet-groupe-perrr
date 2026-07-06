package com.immobilier.abonnementservice.kafka;

import com.immobilier.abonnementservice.config.KafkaConfig;
import com.immobilier.abonnementservice.dto.event.*;
import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import com.immobilier.abonnementservice.utils.PrixAbonnementUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

// Consomme le résultat réel du paiement (topic paiement-traite) et fait évoluer l'abonnement.
// Idempotent via requestId : un replay du même événement ne re-déclenche rien.
@Component
@RequiredArgsConstructor
public class PaiementTraiteConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaiementTraiteConsumer.class);

    private final AbonnementRepository abonnementRepository;
    private final AbonnementEventProducer producer;
    private final EvenementHelper evenements;

    @KafkaListener(topics = KafkaConfig.TOPIC_PAIEMENT_TRAITE, groupId = "abonnement-service")
    @Transactional
    public void consommer(PaiementTraiteEvent event) {
        log.info("paiement-traite reçu : {}", event);

        if (event == null || event.getRequestId() == null) {
            log.warn("Événement paiement-traite invalide, ignoré");
            return;
        }

        Abonnement abonnement = abonnementRepository.findByRequestId(event.getRequestId()).orElse(null);
        if (abonnement == null) {
            log.warn("Aucun abonnement pour requestId={}, événement ignoré", event.getRequestId());
            return;
        }

        boolean succes = "SUCCESS".equalsIgnoreCase(event.getStatut());

        // Idempotence : état déjà atteint -> on acquitte sans rien refaire
        if (succes && abonnement.getStatut() == StatutAbonnement.ACTIF) {
            log.info("Abonnement {} déjà ACTIF, replay ignoré", abonnement.getId());
            return;
        }
        if (!succes && abonnement.getStatut() == StatutAbonnement.ECHEC_PAIEMENT) {
            log.info("Abonnement {} déjà ECHEC_PAIEMENT, replay ignoré", abonnement.getId());
            return;
        }

        if (succes) {
            activer(abonnement, event);
        } else {
            echouer(abonnement, event);
        }
    }

    private void activer(Abonnement a, PaiementTraiteEvent event) {
        LocalDate debut = LocalDate.now();
        a.setStatut(StatutAbonnement.ACTIF);
        a.setDateDebut(debut);
        a.setDateFin(PrixAbonnementUtils.calculerDateFin(debut, a.getType()));
        if (event.getPaiementId() != null) a.setPaiementId(event.getPaiementId());
        if (event.getReference() != null) a.setReferenceTransaction(event.getReference());
        if (event.getModePaiement() != null) a.setModePaiement(event.getModePaiement());
        abonnementRepository.save(a);

        String requestId = a.getRequestId();
        // Renouvellement si l'agent avait déjà un abonnement antérieur
        boolean renouvellement = abonnementRepository.findByUserIdOrderByCreatedAtDesc(a.getUserId()).size() > 1;

        if (renouvellement) {
            producer.abonnementRenewed(AbonnementRenewedEvent.builder()
                    .requestId(requestId).abonnementId(a.getId()).userId(a.getUserId()).email(a.getEmail())
                    .type(a.getType().name()).dateDebut(a.getDateDebut()).dateFin(a.getDateFin()).build());
        } else {
            producer.abonnementActivated(AbonnementActivatedEvent.builder()
                    .requestId(requestId).abonnementId(a.getId()).userId(a.getUserId()).email(a.getEmail())
                    .type(a.getType().name()).dateDebut(a.getDateDebut()).dateFin(a.getDateFin()).build());
        }

        evenements.notifier(requestId, a.getUserId(), a.getEmail(), "PAIEMENT_RECU",
                "Paiement reçu pour votre abonnement " + a.getType() + ".");
        evenements.notifier(requestId, a.getUserId(), a.getEmail(), "ACTIVE",
                "Votre abonnement " + a.getType() + " est actif jusqu'au " + a.getDateFin() + ".");
        evenements.auditer(requestId, renouvellement ? "ABONNEMENT_RENOUVELE" : "ABONNEMENT_ACTIVE",
                a.getId(), a.getUserId(), "Activation abonnement " + a.getType());
    }

    private void echouer(Abonnement a, PaiementTraiteEvent event) {
        a.setStatut(StatutAbonnement.ECHEC_PAIEMENT);
        a.setCommentaire(event.getMessage() != null ? event.getMessage() : "Paiement échoué");
        abonnementRepository.save(a);

        String requestId = a.getRequestId();
        producer.paymentFailed(AbonnementPaymentFailedEvent.builder()
                .requestId(requestId).abonnementId(a.getId()).userId(a.getUserId()).email(a.getEmail())
                .message(a.getCommentaire()).build());
        evenements.notifier(requestId, a.getUserId(), a.getEmail(), "PAIEMENT_ECHOUE",
                "Le paiement de votre abonnement " + a.getType() + " a échoué. Vous pouvez réessayer.");
        evenements.auditer(requestId, "PAIEMENT_ECHEC", a.getId(), a.getUserId(), a.getCommentaire());
    }
}
