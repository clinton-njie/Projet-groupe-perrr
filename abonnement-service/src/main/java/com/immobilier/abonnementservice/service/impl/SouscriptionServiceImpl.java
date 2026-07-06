package com.immobilier.abonnementservice.service.impl;

import com.immobilier.abonnementservice.dto.event.AbonnementCreatedEvent;
import com.immobilier.abonnementservice.dto.request.CreerAbonnementRequest;
import com.immobilier.abonnementservice.dto.request.PaiementRequest;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.dto.response.PaiementResponse;
import com.immobilier.abonnementservice.dto.response.UtilisateurConnecteResponse;
import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.exception.AbonnementException;
import com.immobilier.abonnementservice.exception.RessourceIntrouvableException;
import com.immobilier.abonnementservice.feign.PaiementFeignClient;
import com.immobilier.abonnementservice.kafka.AbonnementEventProducer;
import com.immobilier.abonnementservice.kafka.EvenementHelper;
import com.immobilier.abonnementservice.mapper.AbonnementMapper;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import com.immobilier.abonnementservice.security.JwtService;
import com.immobilier.abonnementservice.service.interfaces.SouscriptionService;
import com.immobilier.abonnementservice.utils.PrixAbonnementUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SouscriptionServiceImpl implements SouscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SouscriptionServiceImpl.class);

    private final JwtService jwtService;
    private final PaiementFeignClient paiementFeignClient;
    private final AbonnementRepository abonnementRepository;
    private final AbonnementEventProducer producer;
    private final EvenementHelper evenements;

    @Override
    @Transactional
    public AbonnementResponse souscrire(String authHeader, CreerAbonnementRequest request) {
        UtilisateurConnecteResponse user = jwtService.getUser(authHeader);
        exigerAgent(user);

        BigDecimal prix = PrixAbonnementUtils.calculerPrix(request.getType());
        String requestId = UUID.randomUUID().toString();

        // 1. Abonnement EN_ATTENTE_PAIEMENT (dates renseignées seulement à l'activation)
        Abonnement abonnement = Abonnement.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .type(request.getType())
                .prix(prix)
                .modePaiement(request.getModePaiement())
                .requestId(requestId)
                .statut(StatutAbonnement.EN_ATTENTE_PAIEMENT)
                .build();
        abonnement = abonnementRepository.save(abonnement);

        // 2. Demande de paiement (accusé seulement) + événements
        demanderPaiement(abonnement, user);

        producer.abonnementCreated(AbonnementCreatedEvent.builder()
                .requestId(requestId).abonnementId(abonnement.getId())
                .userId(user.getId()).email(user.getEmail())
                .type(abonnement.getType().name()).prix(prix)
                .statut(abonnement.getStatut().name()).build());
        evenements.notifier(requestId, user.getId(), user.getEmail(), "ABONNEMENT_CREE",
                "Votre demande d'abonnement " + abonnement.getType() + " a été enregistrée, en attente de paiement.");
        evenements.auditer(requestId, "ABONNEMENT_CREE", abonnement.getId(), user.getId(),
                "Souscription " + abonnement.getType());

        return AbonnementMapper.toResponse(abonnement);
    }

    @Override
    @Transactional
    public AbonnementResponse reessayerPaiement(String authHeader, Long abonnementId) {
        UtilisateurConnecteResponse user = jwtService.getUser(authHeader);
        exigerAgent(user);

        Abonnement abonnement = abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new RessourceIntrouvableException("Abonnement introuvable"));

        if (!abonnement.getUserId().equals(user.getId())) {
            throw new AbonnementException("Cet abonnement ne vous appartient pas");
        }
        if (abonnement.getStatut() != StatutAbonnement.ECHEC_PAIEMENT) {
            throw new AbonnementException("Réessai autorisé uniquement sur un paiement échoué");
        }

        // Nouveau cycle de paiement idempotent
        String requestId = UUID.randomUUID().toString();
        abonnement.setRequestId(requestId);
        abonnement.setStatut(StatutAbonnement.EN_ATTENTE_PAIEMENT);
        abonnement.setCommentaire("Réessai de paiement");
        abonnement = abonnementRepository.save(abonnement);

        demanderPaiement(abonnement, user);
        evenements.notifier(requestId, user.getId(), user.getEmail(), "ABONNEMENT_CREE",
                "Nouvelle tentative de paiement pour votre abonnement " + abonnement.getType() + ".");
        evenements.auditer(requestId, "REESSAI_PAIEMENT", abonnement.getId(), user.getId(), "Réessai paiement");

        return AbonnementMapper.toResponse(abonnement);
    }

    // Demande de paiement (jamais de paiement direct) : on stocke l'accusé {paiementId}.
    // Best-effort : si payment-service est indisponible, l'abonnement reste EN_ATTENTE_PAIEMENT.
    private void demanderPaiement(Abonnement abonnement, UtilisateurConnecteResponse user) {
        try {
            PaiementResponse ack = paiementFeignClient.creerPaiement(PaiementRequest.builder()
                    .requestId(abonnement.getRequestId())
                    .utilisateurId(user.getId())
                    .email(user.getEmail())
                    .montant(abonnement.getPrix())
                    .description("Abonnement " + abonnement.getType())
                    .typePaiement("ABONNEMENT")
                    .modePaiement(abonnement.getModePaiement())
                    .build());
            if (ack != null) {
                abonnement.setPaiementId(ack.getPaiementId());
                if (ack.getReference() != null) {
                    abonnement.setReferenceTransaction(ack.getReference());
                }
                abonnementRepository.save(abonnement);
            }
        } catch (Exception e) {
            log.warn("payment-service indisponible pour requestId={} : {}", abonnement.getRequestId(), e.getMessage());
        }
    }

    private void exigerAgent(UtilisateurConnecteResponse user) {
        if (!"AGENT".equalsIgnoreCase(user.getRole())) {
            throw new AbonnementException("Seuls les agents peuvent souscrire un abonnement");
        }
    }
}
