package com.immobilier.reservationservice.repository;

import com.immobilier.reservationservice.entity.Reservation;
import com.immobilier.reservationservice.enums.ReservationStatus;
import org.springframework.data.repository.query.Param; // Importation de l'annotation @Param pour les paramètres de requête et que les versions de Spring Data JPA plus anciennes nécessitent cette annotation pour lier les paramètres de méthode aux paramètres de requête dans les requêtes JPQL.
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findByStatus(
            ReservationStatus status
    );

    List<Reservation> findByUserId(
            Long userId
    );

    List<Reservation> findByAgentId(
            Long agentId
    );

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.propertyId = :propertyId
        AND r.status IN (
            com.immobilier.reservationservice.enums.ReservationStatus.PENDING,
            com.immobilier.reservationservice.enums.ReservationStatus.APPROVED,
            com.immobilier.reservationservice.enums.ReservationStatus.PAID
        )
        AND (
            r.startDate <= :endDate
            AND
            r.endDate >= :startDate
        )
    """)
    boolean hasConflict(
            @Param("propertyId") Long propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}