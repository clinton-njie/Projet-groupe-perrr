package com.immobilier.abonnementservice.service.impl;

import com.immobilier.abonnementservice.dto.event.AbonnementCreatedEvent;
import com.immobilier.abonnementservice.dto.request.PaiementRequest;
import com.immobilier.abonnementservice.dto.request.RenouvelerAbonnementRequest;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.dto.response.PaiementResponse;
import com.immobilier.abonnementservice.dto.response.UtilisateurConnecteResponse;
import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.exception.AbonnementException;
import com.immobilier.abonnementservice.feign.PaiementFeignClient;
import com.immobilier.abonnementservice.kafka.AbonnementEventProducer;
import com.immobilier.abonnementservice.kafka.EvenementHelper;
import com.immobilier.abonnementservice.mapper.AbonnementMapper;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import com.immobilier.abonnementservice.security.JwtService;
import com.immobilier.abonnementservice.service.interfaces.RenouvellementService;
import com.immobilier.abonnementservice.utils.PrixAbonnementUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RenouvellementServiceImpl implements RenouvellementService {

    private static final Logger log = LoggerFactory.getLogger(RenouvellementServiceImpl.class);

    private final JwtService jwtService;
    private final PaiementFeignClient paiementFeignClient;
    private final AbonnementRepository abonnementRepository;
    private final AbonnementEventProducer producer;
    private final EvenementHelper evenements;

    @Override
    @Transactional
    public AbonnementResponse renouveler(String authHeader, RenouvelerAbonnementRequest request) {
        UtilisateurConnecteResponse user = jwtService.getUser(authHeader);
        if (!"AGENT".equalsIgnoreCase(user.getRole())) {
            throw new AbonnementException("Seuls les agents peuvent renouveler un abonnement");
        }

        // Interdit si un abonnement ACTIF est encore valide ; autorisé si expiré/annulé/absent.
        Optional<Abonnement> dernier = abonnementRepository.findTopByUserIdOrderByDateFinDesc(user.getId());
        if (dernier.isPresent()
                && dernier.get().getStatut() == StatutAbonnement.ACTIF
                && dernier.get().getDateFin() != null
                && !dernier.get().getDateFin().isBefore(LocalDate.now())) {
            throw new AbonnementException("Un abonnement actif est encore valide jusqu'au " + dernier.get().getDateFin());
        }

        BigDecimal prix = PrixAbonnementUtils.calculerPrix(request.getType());
        String requestId = UUID.randomUUID().toString();

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
                .commentaire("Renouvellement")
                .build();
        abonnement = abonnementRepository.save(abonnement);

        // Demande de paiement (accusé) — best-effort
        try {
            PaiementResponse ack = paiementFeignClient.creerPaiement(PaiementRequest.builder()
                    .requestId(requestId).utilisateurId(user.getId()).email(user.getEmail())
                    .montant(prix).description("Renouvellement " + request.getType())
                    .typePaiement("ABONNEMENT").modePaiement(request.getModePaiement()).build());
            if (ack != null) {
                abonnement.setPaiementId(ack.getPaiementId());
                abonnement.setReferenceTransaction(ack.getReference());
                abonnement = abonnementRepository.save(abonnement);
            }
        } catch (Exception e) {
            log.warn("payment-service indisponible (renouvellement) requestId={} : {}", requestId, e.getMessage());
        }

        producer.abonnementCreated(AbonnementCreatedEvent.builder()
                .requestId(requestId).abonnementId(abonnement.getId())
                .userId(user.getId()).email(user.getEmail())
                .type(request.getType().name()).prix(prix)
                .statut(abonnement.getStatut().name()).build());
        evenements.notifier(requestId, user.getId(), user.getEmail(), "RENOUVELLEMENT",
                "Votre demande de renouvellement " + request.getType() + " est en attente de paiement.");
        evenements.auditer(requestId, "RENOUVELLEMENT_DEMANDE", abonnement.getId(), user.getId(),
                "Renouvellement " + request.getType());

        return AbonnementMapper.toResponse(abonnement);
    }
}
