package com.immobilier.abonnementservice.repository;

import com.immobilier.abonnementservice.entity.Abonnement;
import com.immobilier.abonnementservice.enums.StatutAbonnement;
import com.immobilier.abonnementservice.enums.TypeAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    // Dernier abonnement d'un agent (le plus récent en date de fin) : sert au statut / renouvellement
    Optional<Abonnement> findTopByUserIdOrderByDateFinDesc(Long userId);

    // Historique d'un agent, du plus récent au plus ancien
    List<Abonnement> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Corrélation idempotente (consumer Kafka paiement-traite)
    Optional<Abonnement> findByRequestId(String requestId);

    Optional<Abonnement> findFirstByUserIdAndStatutOrderByDateFinDesc(Long userId, StatutAbonnement statut);

    List<Abonnement> findByStatut(StatutAbonnement statut);

    long countByStatut(StatutAbonnement statut);

    long countByType(TypeAbonnement type);

    // Expiration : abonnements actifs dont la date de fin est dépassée
    List<Abonnement> findByDateFinBeforeAndStatut(LocalDate date, StatutAbonnement statut);

    // Rappel "expiration proche" : actifs expirant à une date donnée
    List<Abonnement> findByDateFinAndStatut(LocalDate date, StatutAbonnement statut);

    // Stats par période (sur la date de création)
    long countByCreatedAtBetween(java.time.LocalDateTime debut, java.time.LocalDateTime fin);

    List<Abonnement> findByCreatedAtBetween(java.time.LocalDateTime debut, java.time.LocalDateTime fin);
}
