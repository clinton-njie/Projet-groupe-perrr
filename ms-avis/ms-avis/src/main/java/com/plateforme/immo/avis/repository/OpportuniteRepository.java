package com.plateforme.immo.avis.repository;

import com.plateforme.immo.avis.model.OpportuniteNotation;
import com.plateforme.immo.avis.model.StatutOpportunite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OpportuniteRepository extends JpaRepository<OpportuniteNotation, Long> {
    List<OpportuniteNotation> findByClientIdAndStatut(Long clientId, StatutOpportunite statut);
    Optional<OpportuniteNotation> findByTransactionId(Long transactionId);
}