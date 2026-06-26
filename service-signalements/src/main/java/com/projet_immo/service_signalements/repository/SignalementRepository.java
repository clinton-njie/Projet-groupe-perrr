package com.projet_immo.service_signalements.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.projet_immo.service_signalements.entity.Signalement;

@Repository
public interface SignalementRepository extends JpaRepository<Signalement,Long>{

List<Signalement> findByUtilisateurId(Long id);
Long countByUtilisateurIdAndDateSignalement(Long utilisateurId, LocalDate dateSignalement);

}
