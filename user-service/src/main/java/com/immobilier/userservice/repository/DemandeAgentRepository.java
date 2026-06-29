package com.immobilier.userservice.repository;

import com.immobilier.userservice.entity.DemandeAgent;
import com.immobilier.userservice.enums.StatutDemande;
import com.immobilier.userservice.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository pour l'entité DemandeAgent, fournissant des méthodes de recherche personnalisées.
public interface DemandeAgentRepository extends JpaRepository<DemandeAgent, Long> {

    // Toutes les demandes d’un utilisateur
    List<DemandeAgent> findByUtilisateur(Utilisateur utilisateur);

    // Filtrer par statut (EN_ATTENTE, VALIDEE, REFUSEE)
    List<DemandeAgent> findByStatut(StatutDemande statut);

    // Vérifier s’il existe une demande en attente
    boolean existsByUtilisateurAndStatut(Utilisateur utilisateur, StatutDemande statut);
}