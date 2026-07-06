package com.immobilier.reservationservice.service;

import com.immobilier.reservationservice.client.PaymentClient;
import com.immobilier.reservationservice.client.PropertyClient;
import com.immobilier.reservationservice.client.UserClient;
import com.immobilier.reservationservice.dto.ReservationEvent;
import com.immobilier.reservationservice.dto.ReservationRequest;
import com.immobilier.reservationservice.dto.ReservationResponse;
import com.immobilier.reservationservice.dto.TransactionTermineeEvent;
import com.immobilier.reservationservice.dto.external.PaymentRequestDto;
import com.immobilier.reservationservice.dto.external.PaymentResponseDto;
import com.immobilier.reservationservice.dto.external.PropertyDto;
import com.immobilier.reservationservice.dto.external.StatutBienUpdateRequest;
import com.immobilier.reservationservice.dto.external.UserDto;
import com.immobilier.reservationservice.entity.Reservation;
import com.immobilier.reservationservice.enums.ReservationStatus;
import com.immobilier.reservationservice.exception.BusinessException;
import com.immobilier.reservationservice.kafka.ReservationEventProducer;
import com.immobilier.reservationservice.mapper.ReservationMapper;
import com.immobilier.reservationservice.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Pas de @Transactional de classe : createReservation() appelle payment-service en synchrone,
// qui rappelle aussitôt PUT /api/reservations/{id}/confirmer -- la réservation doit donc être
// committée avant cet appel sortant, pas retenue dans une transaction encore ouverte.
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository repository;
    private final UserClient userClient;
    private final PaymentClient paymentClient;
    private final PropertyClient propertyClient;
    private final ReservationEventProducer producer;
    private final ReservationMapper mapper;

    @Override
    public ReservationResponse createReservation(Long userId, ReservationRequest request) {

        UserDto user = userClient.getUserById(userId);
        if (user == null) {
            throw new BusinessException("Utilisateur introuvable");
        }

        PropertyDto property = propertyClient.getProperty(request.getPropertyId());
        if (property == null) {
            throw new BusinessException("Bien introuvable");
        }

        boolean conflict = repository.hasConflict(property.getId(), request.getStartDate(), request.getEndDate());
        if (conflict) {
            throw new BusinessException("Bien déjà réservé sur cette période");
        }

        Double amount = property.getPrixMensuel().doubleValue() * computeMonths(request.getStartDate(), request.getEndDate());
        String nomUtilisateur = (user.getFirstname() + " " + user.getLastname()).trim();

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setPropertyId(property.getId());
        reservation.setAgentId(resolveAgentId(property.getAgentEmail()));
        reservation.setStartDate(request.getStartDate());
        reservation.setEndDate(request.getEndDate());
        reservation.setAmount(amount);
        reservation.setStatus(ReservationStatus.EN_ATTENTE);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setOperateur(request.getOperateur());
        reservation.setTelephone(request.getTelephone());
        reservation.setNomBien(property.getNom());
        reservation.setNomUtilisateur(nomUtilisateur);

        reservation = repository.save(reservation);

        PaymentResponseDto paymentResponse;
        try {
            paymentResponse = paymentClient.initiatePayment(PaymentRequestDto.builder()
                    .referenceId(reservation.getId())
                    .referenceType("RESERVATION")
                    .nomUtilisateur(nomUtilisateur)
                    .nomBien(reservation.getNomBien())
                    .montant(amount)
                    .operateur(request.getOperateur())
                    .telephone(request.getTelephone())
                    .build());
        } catch (Exception e) {
            log.error("Echec appel payment-service pour la reservation {}", reservation.getId(), e);
            throw new BusinessException("Paiement indisponible, réessayez");
        }

        if (paymentResponse == null) {
            throw new BusinessException("Paiement indisponible, réessayez");
        }

        // Mise à jour ciblée du seul paymentId (voir ReservationRepository.updatePaymentId) :
        // le callback synchrone confirmer() a pu déjà changer le statut entre-temps sur une
        // autre requête -- un save() de l'objet "reservation" ici l'écraserait avec la valeur
        // obsolète tenue par le cache de 1er niveau Hibernate de cette requête.
        repository.updatePaymentId(reservation.getId(), paymentResponse.getPaymentId());
        reservation.setPaymentId(paymentResponse.getPaymentId());

        publishEvent(ReservationEvent.builder()
                .eventType("RESERVATION_CREATED")
                .reservationId(reservation.getId())
                .userId(reservation.getUserId())
                .propertyId(reservation.getPropertyId())
                .message("Nouvelle réservation créée")
                .build());

        return mapper.toResponse(reservation);
    }

    @Override
    @Transactional
    public ReservationResponse confirmReservation(Long id) {

        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Réservation introuvable"));

        reservation.setStatus(ReservationStatus.CONFIRME);
        Reservation saved = repository.save(reservation);

        propertyClient.changerStatut(saved.getPropertyId(), new StatutBienUpdateRequest("LOUE"));

        publishEvent(ReservationEvent.builder()
                .eventType("RESERVATION_CONFIRMED")
                .reservationId(saved.getId())
                .userId(saved.getUserId())
                .propertyId(saved.getPropertyId())
                .message("Réservation confirmée")
                .build());

        // Transaction terminée avec succès -> ms-avis crée l'opportunité de notation.
        publishTransactionTerminee(saved);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long id) {

        Reservation reservation = repository.findById(id)
                .orElseThrow(() -> new BusinessException("Réservation introuvable"));

        reservation.setStatus(ReservationStatus.ANNULE);
        Reservation saved = repository.save(reservation);

        propertyClient.changerStatut(saved.getPropertyId(), new StatutBienUpdateRequest("DISPONIBLE"));

        publishEvent(ReservationEvent.builder()
                .eventType("RESERVATION_CANCELLED")
                .reservationId(saved.getId())
                .userId(saved.getUserId())
                .propertyId(saved.getPropertyId())
                .message("Réservation annulée")
                .build());

        return mapper.toResponse(saved);
    }

    private long computeMonths(LocalDate start, LocalDate end) {
        if (!end.isAfter(start)) {
            throw new BusinessException("Dates invalides");
        }
        long moisEntiers = ChronoUnit.MONTHS.between(start, end);
        LocalDate finMoisEntiers = start.plusMonths(moisEntiers);
        if (finMoisEntiers.isBefore(end)) {
            moisEntiers += 1; // mois partiel arrondi au-dessus
        }
        return Math.max(moisEntiers, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long reservationId) {

        Reservation reservation = repository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("Réservation introuvable"));

        return mapper.toResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByUser(Long userId) {

        return repository.findByUserId(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeInteraction(Long utilisateurId, Long agentId) {
        return repository.existeInteraction(utilisateurId, agentId);
    }

    @Override
    public void expireOldReservations() {

        List<Reservation> reservationsEnAttente = repository.findByStatus(ReservationStatus.EN_ATTENTE);

        for (Reservation reservation : reservationsEnAttente) {

            if (reservation.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {

                reservation.setStatus(ReservationStatus.EXPIRE);
                repository.save(reservation);

                publishEvent(ReservationEvent.builder()
                        .eventType("RESERVATION_EXPIRED")
                        .reservationId(reservation.getId())
                        .userId(reservation.getUserId())
                        .message("Réservation expirée")
                        .build());
            }
        }
    }

    // Best-effort : un souci Kafka (broker indisponible, etc.) ne doit pas faire échouer
    // l'opération métier déjà commitée en base.
    private void publishEvent(ReservationEvent event) {
        try {
            producer.publish(event);
        } catch (Exception e) {
            log.warn("Echec publication événement Kafka {} pour la réservation {}", event.getEventType(), event.getReservationId(), e);
        }
    }

    // Best-effort : la notation est secondaire, un souci Kafka ne doit pas casser la confirmation.
    private void publishTransactionTerminee(Reservation reservation) {
        try {
            producer.publishTransactionTerminee(TransactionTermineeEvent.builder()
                    .transactionId(reservation.getId())
                    .clientId(reservation.getUserId())
                    .agentId(reservation.getAgentId())
                    .bienId(reservation.getPropertyId())
                    .typeTransaction("RESERVATION")
                    .build());
        } catch (Exception e) {
            log.warn("Echec publication transaction.terminee pour la réservation {}", reservation.getId(), e);
        }
    }

    // Résout l'id numérique de l'agent à partir de son email (exposé par bien-service).
    // Best-effort : un agent introuvable ne doit pas empêcher la réservation ; agentId restera null.
    private Long resolveAgentId(String agentEmail) {
        if (agentEmail == null || agentEmail.isBlank()) {
            return null;
        }
        try {
            return userClient.getAuthUserIdByEmail(agentEmail);
        } catch (Exception e) {
            log.warn("Impossible de résoudre l'agentId pour l'email {}", agentEmail, e);
            return null;
        }
    }
}
