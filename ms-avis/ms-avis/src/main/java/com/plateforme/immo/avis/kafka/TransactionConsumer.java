package com.plateforme.immo.avis.kafka;

import com.plateforme.immo.avis.config.KafkaConfig;
import com.plateforme.immo.avis.dto.TransactionInterneDTO;
import com.plateforme.immo.avis.model.TypeTransaction;
import com.plateforme.immo.avis.service.OpportuniteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Consomme les événements publiés par ms-reservation / ms-visite lorsqu'une
 * transaction passe à TERMINÉE, et crée l'opportunité de notation associée.
 */
@Component
public class TransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

    private final OpportuniteService opportuniteService;

    public TransactionConsumer(OpportuniteService opportuniteService) {
        this.opportuniteService = opportuniteService;
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_TRANSACTION_TERMINEE,
            groupId = "ms-avis"
    )
    public void consommer(TransactionInterneDTO event) {
        log.info("Événement '{}' reçu : {}", KafkaConfig.TOPIC_TRANSACTION_TERMINEE, event);

        final TypeTransaction type;
        try {
            type = TypeTransaction.valueOf(event.getTypeTransaction());
        } catch (IllegalArgumentException | NullPointerException e) {
            // Message mal formé : on l'acquitte pour ne pas boucler indéfiniment.
            log.error("typeTransaction invalide, événement ignoré : {}", event.getTypeTransaction());
            return;
        }

        try {
            opportuniteService.creerOpportunite(
                    event.getClientId(),
                    event.getAgentId(),
                    event.getBienId(),
                    event.getTransactionId(),
                    type
            );
            log.info("Opportunité de notation créée pour la transaction {}", event.getTransactionId());
        } catch (ResponseStatusException e) {
            // Unicité : une opportunité existe déjà pour cette transaction.
            // On acquitte (log) sans relancer d'exception pour éviter une boucle de replay.
            log.warn("Opportunité déjà existante pour la transaction {} : {}",
                    event.getTransactionId(), e.getReason());
        }
    }
}
