package com.immobilier.reservationservice.repository;

import com.immobilier.reservationservice.entity.Reservation;
import com.immobilier.reservationservice.enums.ReservationStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    // Mise à jour ciblée (pas de save() de l'entité entière) : createReservation() garde son
    // EntityManager ouvert (open-in-view) pendant tout l'appel synchrone à payment-service, qui
    // rappelle entre-temps confirmReservation()/cancelReservation() sur une AUTRE requête -- un
    // save() de l'objet en mémoire à ce stade écraserait ce changement de statut concurrent avec
    // les valeurs obsolètes du 1er niveau de cache Hibernate. @Transactional ici (et non sur
    // createReservation) : une requête @Modifying exige une transaction active, mais on ne veut
    // pas que celle-ci s'étende à l'appel payment-service qui l'entoure.
    @Transactional
    @Modifying
    @Query("UPDATE Reservation r SET r.paymentId = :paymentId WHERE r.id = :id")
    void updatePaymentId(@Param("id") Long id, @Param("paymentId") String paymentId);

    List<Reservation> findByStatus(
            ReservationStatus status
    );

    List<Reservation> findByUserId(
            Long userId
    );

    // Interaction éligible à un signalement d'agent : réservation CONFIRME ou EXPIRE.
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.userId = :utilisateurId
        AND r.agentId = :agentId
        AND r.status IN (
            com.immobilier.reservationservice.enums.ReservationStatus.CONFIRME,
            com.immobilier.reservationservice.enums.ReservationStatus.EXPIRE
        )
    """)
    boolean existeInteraction(
            @Param("utilisateurId") Long utilisateurId,
            @Param("agentId") Long agentId
    );

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.propertyId = :propertyId
        AND r.status IN (
            com.immobilier.reservationservice.enums.ReservationStatus.EN_ATTENTE,
            com.immobilier.reservationservice.enums.ReservationStatus.CONFIRME
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
