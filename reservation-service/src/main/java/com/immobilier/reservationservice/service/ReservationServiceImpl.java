package com.immobilier.reservationservice.service;

import com.immobilier.reservationservice.client.PaymentClient;
import com.immobilier.reservationservice.client.PropertyClient;
import com.immobilier.reservationservice.client.UserClient;
import com.immobilier.reservationservice.dto.ReservationEvent;
import com.immobilier.reservationservice.dto.ReservationRequest;
import com.immobilier.reservationservice.dto.ReservationResponse;
import com.immobilier.reservationservice.dto.external.PaymentRequestDto;
import com.immobilier.reservationservice.dto.external.PaymentResponseDto;
import com.immobilier.reservationservice.dto.external.PropertyDto;
import com.immobilier.reservationservice.dto.external.UserDto;
import com.immobilier.reservationservice.entity.Reservation;
import com.immobilier.reservationservice.enums.ReservationStatus;
import com.immobilier.reservationservice.exception.BusinessException;
import com.immobilier.reservationservice.kafka.ReservationEventProducer;
import com.immobilier.reservationservice.mapper.ReservationMapper;
import com.immobilier.reservationservice.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl
        implements ReservationService {

        private final ReservationRepository repository;
        private final UserClient userClient;
        private final PaymentClient paymentClient;
        private final PropertyClient propertyClient;
        private final PriceCalculatorService priceCalculator;
        private final ReservationEventProducer producer;
        private final ReservationMapper mapper;
        // Kafka (plus tard)
        // private final ReservationEventProducer producer;

        @Override
    public ReservationResponse createReservation(
            Long userId,
            ReservationRequest request
    ) {

        UserDto user =
                userClient.getUserById(userId);

        if (user == null) {

            throw new BusinessException(
                    "Utilisateur introuvable"
            );
        }

        PropertyDto property =
                propertyClient.getProperty(
                        request.getPropertyId()
                );

        if (property == null) {

            throw new BusinessException(
                    "Bien introuvable"
            );
        }

        boolean conflict =
                repository.hasConflict(
                        property.getId(),
                        request.getStartDate(),
                        request.getEndDate()
                );

        if (conflict) {

            throw new BusinessException(
                    "Bien déjà réservé sur cette période"
            );
        }

        Double amount =
                priceCalculator.calculatePrice(
                        property.getDailyPrice(),
                        request.getStartDate(),
                        request.getEndDate()
                );

        Reservation reservation =
                new Reservation();

        reservation.setUserId(userId);

        reservation.setPropertyId(property.getId());

        reservation.setStartDate(
                request.getStartDate()
        );

        reservation.setEndDate(
                request.getEndDate()
        );

        reservation.setAmount(amount);

        reservation.setStatus(
                ReservationStatus.PENDING
        );

        reservation.setCreatedAt(
                LocalDateTime.now()
        );

        repository.save(reservation);

        producer.publish(

            ReservationEvent.builder()
                .eventType("RESERVATION_CREATED")
                .reservationId(reservation.getId())
                .userId(reservation.getUserId())
                .propertyId(reservation.getPropertyId())
                .message("Nouvelle réservation créée")
                .build()
        );

        return mapToResponse(reservation);
    }

        @Override
        @Transactional
        public ReservationResponse confirmReservation(
                Long id) {

        Reservation reservation = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Réservation introuvable"));

        reservation.setStatus(
                ReservationStatus.CONFIRMED);

        Reservation saved =
                repository.save(reservation);

        return mapper.toResponse(saved);
        }

        @Override
        @Transactional
        public ReservationResponse cancelReservation(
                Long id) {

        Reservation reservation = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Réservation introuvable"));

        reservation.setStatus(
                ReservationStatus.CANCELLED);

        Reservation saved =
                repository.save(reservation);

        return mapper.toResponse(saved);
        }

        @Override
        @Transactional
        public ReservationResponse initiatePayment(

                Long reservationId) {

        Reservation reservation =
                repository.findById(reservationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Réservation introuvable"));

        if (reservation.getStatus() !=
                ReservationStatus.APPROVED) {

                throw new RuntimeException(
                        "La réservation doit être APPROVED.");
        }

        reservation.setStatus(
                ReservationStatus.PAYMENT_PENDING
        );

        Reservation saved =
                repository.save(
                        reservation
                );

        PaymentRequestDto request =
                PaymentRequestDto.builder()
                        .reservationId(saved.getId())
                        .userId(saved.getUserId())
                        .propertyId(saved.getPropertyId())
                        .amount(calculatePrice(saved.getAmount(),saved.getStartDate(),saved.getEndDate()))
                        .build();

        PaymentResponseDto paymentResponse =
                paymentClient.initiatePayment(request);

                if (paymentResponse == null) {
                throw new RuntimeException("Échec de l'initialisation du paiement.");
                }

                return mapper.toResponse(saved);
        }

     public double calculatePrice(
            Double monthlyPrice,
            LocalDate startDate,
            LocalDate endDate
    ) {

        long days =
                ChronoUnit.DAYS.between(
                        startDate,
                        endDate
                );

        if(days <= 0){
            throw new IllegalArgumentException(
                    "Dates invalides"
            );
        }

        return (monthlyPrice/30) * days;
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(
            Long reservationId
    ) {

        Reservation reservation =
                repository.findById(reservationId)
                        .orElseThrow(
                                () -> new BusinessException(
                                        "Réservation introuvable"
                                )
                        );

        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse>
    getReservationsByUser(Long userId) {

        return repository.findAll()
                .stream()
                .filter(r -> r.getUserId().equals(userId))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse>
    getPendingReservations() {

        return repository
                .findByStatus(
                        ReservationStatus.PENDING
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ReservationResponse approve(
            Long reservationId,
            Long agentId
    ) {

        Reservation reservation =
                repository.findById(
                        reservationId
                ).orElseThrow(
                        () -> new BusinessException(
                                "Réservation introuvable"
                        )
                );

        reservation.setStatus(
                ReservationStatus.APPROVED
        );

        reservation.setAgentId(agentId);

        reservation.setReviewedAt(
                LocalDateTime.now()
        );

        repository.save(reservation);
        // Kafka :
        //producer.publish(APPROVED)
        producer.publish(

            ReservationEvent.builder()
                .eventType("RESERVATION_CREATED")
                .reservationId(reservation.getId())
                .userId(reservation.getUserId())
                .propertyId(reservation.getPropertyId())
                .message("Nouvelle réservation créée")
                .build()
         );

        return mapToResponse(reservation);
    }

    @Override
    public ReservationResponse reject(
            Long reservationId,
            Long agentId
    ) {

        Reservation reservation =
                repository.findById(
                        reservationId
                ).orElseThrow(
                        () -> new BusinessException(
                                "Réservation introuvable"
                        )
                );

        reservation.setStatus(
                ReservationStatus.REJECTED
        );

        reservation.setAgentId(agentId);

        reservation.setReviewedAt(
                LocalDateTime.now()
        );

        repository.save(reservation);

         producer.publish(

            ReservationEvent.builder()
                .eventType("RESERVATION_REJECTED")
                .reservationId(reservation.getId())
                .userId(reservation.getUserId())
                .agentId(agentId)
                .message("Réservation rejetée")
                .build()
        );

        return mapToResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse>
    getAllReservations() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void expireOldReservations() {

        List<Reservation> pendingReservations =
                repository.findByStatus(
                        ReservationStatus.PENDING
                );

        for (Reservation reservation
                : pendingReservations) {

            if (reservation.getCreatedAt()
                    .isBefore(
                            LocalDateTime.now()
                                    .minusHours(24)
                    )) {

                reservation.setStatus(
                        ReservationStatus.EXPIRED
                );

                repository.save(reservation);

            producer.publish(

                ReservationEvent.builder()
                    .eventType("RESERVATION_EXPIRED")
                    .reservationId(reservation.getId())
                    .userId(reservation.getUserId())
                    .message("Réservation expirée")
                    .build()
            );
            }
        }
    }

    private ReservationResponse mapToResponse(
        Reservation reservation) {

        ReservationResponse response =
                new ReservationResponse();

        response.setId(
                reservation.getId()
        );

        response.setUserId(
                reservation.getUserId()
        );

        response.setPropertyId(
                reservation.getPropertyId()
        );

        response.setStartDate(
                reservation.getStartDate()
        );

        response.setEndDate(
                reservation.getEndDate()
        );

        response.setAmount(
                reservation.getAmount()
        );

        response.setStatus(
                reservation.getStatus().name()
        );

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse>
    getReservationsByAgent(Long agentId) {

        return repository
                .findByAgentId(agentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

}  