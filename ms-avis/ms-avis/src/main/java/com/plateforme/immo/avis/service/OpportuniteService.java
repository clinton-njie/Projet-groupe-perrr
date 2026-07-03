package com.plateforme.immo.avis.service;

import com.plateforme.immo.avis.model.OpportuniteNotation;
import com.plateforme.immo.avis.model.StatutOpportunite;
import com.plateforme.immo.avis.model.TypeTransaction;
import com.plateforme.immo.avis.repository.OpportuniteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OpportuniteService {

    @Autowired
    private OpportuniteRepository opportuniteRepository;

    public void creerOpportunite(Long clientId, Long agentId, Long bienId, Long transactionId, TypeTransaction type) {
        if (opportuniteRepository.findByTransactionId(transactionId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une opportunité existe déjà pour cette transaction");
        }

        OpportuniteNotation opp = OpportuniteNotation.builder()
                .clientId(clientId)
                .agentId(agentId)
                .bienId(bienId)
                .transactionId(transactionId)
                .typeTransaction(type)
                .statut(StatutOpportunite.EN_ATTENTE)
                .dateCreation(LocalDateTime.now())
                .dateExpiration(LocalDateTime.now().plusDays(30))
                .build();

        opportuniteRepository.save(opp);
    }

    public List<OpportuniteNotation> getEnAttente(Long clientId) {
        return opportuniteRepository.findByClientIdAndStatut(clientId, StatutOpportunite.EN_ATTENTE)
                .stream()
                .filter(opp -> opp.getDateExpiration() == null || opp.getDateExpiration().isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());
    }
}