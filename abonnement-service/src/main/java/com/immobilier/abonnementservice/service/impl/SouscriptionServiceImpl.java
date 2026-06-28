package com.immobilier.abonnementservice.service.impl;

import com.immobilier.abonnementservice.dto.request.CreerAbonnementRequest;
import com.immobilier.abonnementservice.dto.request.PaiementRequest;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.dto.response.PaiementResponse;
import com.immobilier.abonnementservice.dto.response.UtilisateurConnecteResponse;
import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.feign.PaiementFeignClient;
import com.immobilier.abonnementservice.mapper.AbonnementMapper;
import com.immobilier.abonnementservice.repository.AbonnementRepository;
import com.immobilier.abonnementservice.security.JwtService;
import com.immobilier.abonnementservice.service.interfaces.SouscriptionService;
import com.immobilier.abonnementservice.utils.PrixAbonnementUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SouscriptionServiceImpl implements SouscriptionService {

    private final JwtService jwtService;
    private final PaiementFeignClient paiementFeignClient;
    private final AbonnementRepository abonnementRepository;
    //private final AbonnementProducer abonnementProducer;

    @Override
    public AbonnementResponse souscrire(String token, CreerAbonnementRequest request) {

        // 1. Utilisateur connecté
        UtilisateurConnecteResponse user = jwtService.getUser(token);

        if (!"AGENT".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Seuls les agents peuvent souscrire");
        }

        // 2. Paiement request
        PaiementRequest paiementRequest = PaiementRequest.builder()
                .utilisateurId(user.getId())
                .montant(PrixAbonnementUtils.calculerPrix(request.getType()))
                .description("Abonnement " + request.getType())
                .typePaiement("ABONNEMENT")
                .build();

        PaiementResponse paiement = paiementFeignClient.creerPaiement(paiementRequest);

        if (paiement == null || !"SUCCESS".equalsIgnoreCase(paiement.getStatut())) {
            throw new RuntimeException("Paiement échoué");
        }

        // 3. Création abonnement
        LocalDate debut = LocalDate.now();

        Abonnement abonnement = Abonnement.builder()
                .agentId(user.getId())
                .type(request.getType())
                .prix(PrixAbonnementUtils.calculerPrix(request.getType()))
                .dateDebut(debut)
                .dateFin(PrixAbonnementUtils.calculerDateFin(debut, request.getType()))
                .statut(StatutAbonnement.ACTIF)
                .paiementId(paiement.getId())
                .referenceTransaction(paiement.getReference())
                .build();

        abonnementRepository.save(abonnement);

        /*abonnementProducer.sendAbonnementCreated(
            abonnement.getId(),
            abonnement.getAgentId(),
            abonnement.getType().name(),
            abonnement.getStatut().name()
        ); */


        return AbonnementMapper.toResponse(abonnement);
    }
}