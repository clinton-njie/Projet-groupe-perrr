package com.projet_immo.service_signalements.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projet_immo.service_signalements.entity.Signalement;
import com.projet_immo.service_signalements.enums.SignalementStatut;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement,Long>{

List<Signalement> findByUtilisateurId(Long id);
Long countByUtilisateurIdAndDateSignalement(Long utilisateurId, LocalDate dateSignalement);

// Un utilisateur ne peut pas avoir 2 signalements EN_ATTENTE contre le même agent
// avec le même objet de signalement.
boolean existsByUtilisateurIdAndAgentIdAndObjetSignalement_IdAndStatut(
        Long utilisateurId, Long agentId, Long objetSignalementId, SignalementStatut statut);

}
