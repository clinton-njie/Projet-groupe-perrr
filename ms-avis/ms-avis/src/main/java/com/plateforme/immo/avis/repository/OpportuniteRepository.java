package com.plateforme.immo.avis.repository;

import com.plateforme.immo.avis.model.OpportuniteNotation;
import com.plateforme.immo.avis.model.StatutOpportunite;
import com.plateforme.immo.avis.model.TypeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OpportuniteRepository extends JpaRepository<OpportuniteNotation, Long> {
    List<OpportuniteNotation> findByClientIdAndStatut(Long clientId, StatutOpportunite statut);
    // Unicité d'une opportunité par (transaction, type) : une visite et une réservation
    // peuvent partager le même id (bases séparées) sans être considérées comme un doublon.
    Optional<OpportuniteNotation> findByTransactionIdAndTypeTransaction(Long transactionId, TypeTransaction typeTransaction);
}