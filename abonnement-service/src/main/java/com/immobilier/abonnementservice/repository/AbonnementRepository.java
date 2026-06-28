package com.immobilier.abonnementservice.repository;

import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.enums.TypeAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    Optional<Abonnement> findTopByAgentIdOrderByDateFinDesc(Long agentId);

    List<Abonnement> findByAgentIdOrderByCreatedAtDesc(Long agentId);

    List<Abonnement> findByStatut(StatutAbonnement statut);

    long countByStatut(StatutAbonnement statut);

    long countByType(TypeAbonnement type);

    List<Abonnement> findByDateFinBeforeAndStatut(LocalDate date, StatutAbonnement statut);

}