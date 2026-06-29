package com.immobilier.userservice.repository;

import com.immobilier.userservice.entity.SuppressionCompte;
import com.immobilier.userservice.entity.Utilisateur;
import com.immobilier.userservice.enums.StatutSuppression;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Repository pour l'entité SuppressionCompte, fournissant des méthodes de recherche personnalisées.
public interface SuppressionCompteRepository extends JpaRepository<SuppressionCompte, Long> {

    // Récupérer suppression par utilisateur
    Optional<SuppressionCompte> findByUtilisateur(Utilisateur utilisateur);

    // Liste par statut (EN_ATTENTE, ANNULEE, SUPPRIMEE)
    List<SuppressionCompte> findByStatut(StatutSuppression statut);
}