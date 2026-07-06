package com.immobilier.reservationservice.service;

import com.immobilier.reservationservice.client.PaymentClient;
import com.immobilier.reservationservice.client.PropertyClient;
import com.immobilier.reservationservice.client.UserClient;
import com.immobilier.reservationservice.dto.ReservationRequest;
import com.immobilier.reservationservice.dto.ReservationResponse;
import com.immobilier.reservationservice.dto.external.PaymentResponseDto;
import com.immobilier.reservationservice.dto.external.PropertyDto;
import com.immobilier.reservationservice.dto.external.UserDto;
import com.immobilier.reservationservice.entity.Reservation;
import com.immobilier.reservationservice.enums.OperateurMobileMoney;
import com.immobilier.reservationservice.enums.ReservationStatus;
import com.immobilier.reservationservice.exception.BusinessException;
import com.immobilier.reservationservice.kafka.ReservationEventProducer;
import com.immobilier.reservationservice.mapper.ReservationMapper;
import com.immobilier.reservationservice.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository repository;

    @Mock
    private UserClient userClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private PropertyClient propertyClient;

    @Mock
    private ReservationEventProducer producer;

    private ReservationServiceImpl reservationService;

    private ReservationRequest request() {
        ReservationRequest request = new ReservationRequest();
        request.setPropertyId(10L);
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setEndDate(LocalDate.of(2026, 3, 1));
        request.setOperateur(OperateurMobileMoney.ORANGE);
        request.setTelephone("690000000");
        return request;
    }

    private UserDto user() {
        UserDto user = new UserDto();
        user.setAuthUserId(5L);
        user.setFirstname("Jean");
        user.setLastname("Dupont");
        return user;
    }

    private PropertyDto property() {
        PropertyDto property = new PropertyDto();
        property.setId(10L);
        property.setNom("Villa Bella");
        property.setPrixMensuel(100000f);
        property.setStatut("DISPONIBLE");
        return property;
    }

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(
                repository, userClient, paymentClient, propertyClient, producer, new ReservationMapper());
    }

    @Test
    void createReservation_utilisateurIntrouvable_leveException() {
        when(userClient.getUserById(5L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> reservationService.createReservation(5L, request()));
        verify(repository, never()).save(any());
    }

    @Test
    void createReservation_bienIntrouvable_leveException() {
        when(userClient.getUserById(5L)).thenReturn(user());
        when(propertyClient.getProperty(10L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> reservationService.createReservation(5L, request()));
        verify(repository, never()).save(any());
    }

    @Test
    void createReservation_conflitDeDates_leveException() {
        when(userClient.getUserById(5L)).thenReturn(user());
        when(propertyClient.getProperty(10L)).thenReturn(property());
        when(repository.hasConflict(eq(10L), any(), any())).thenReturn(true);

        assertThrows(BusinessException.class, () -> reservationService.createReservation(5L, request()));
        verify(repository, never()).save(any());
    }

    @Test
    void createReservation_paiementIndisponible_leveExceptionEtNePublieRien() {
        when(userClient.getUserById(5L)).thenReturn(user());
        when(propertyClient.getProperty(10L)).thenReturn(property());
        when(repository.hasConflict(eq(10L), any(), any())).thenReturn(false);
        when(repository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(paymentClient.initiatePayment(any())).thenThrow(new RuntimeException("payment-service down"));

        assertThrows(BusinessException.class, () -> reservationService.createReservation(5L, request()));
        verify(producer, never()).publish(any());
    }

    @Test
    void createReservation_calculeLeMontantSurLesMoisEtPublieEvenement() {
        when(userClient.getUserById(5L)).thenReturn(user());
        when(propertyClient.getProperty(10L)).thenReturn(property());
        when(repository.hasConflict(eq(10L), any(), any())).thenReturn(false);
        when(repository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });
        PaymentResponseDto paymentResponse = new PaymentResponseDto();
        paymentResponse.setPaymentId("PAY-1");
        paymentResponse.setStatut("EN_ATTENTE");
        when(paymentClient.initiatePayment(any())).thenReturn(paymentResponse);

        ReservationResponse response = reservationService.createReservation(5L, request());

        // 1er janvier -> 1er mars = 2 mois pleins
        assertEquals(200000.0, response.getAmount());
        assertEquals("Jean Dupont", response.getNomUtilisateur());
        assertEquals(ReservationStatus.EN_ATTENTE.name(), response.getStatus());
        verify(repository, times(1)).save(any(Reservation.class));
        verify(repository).updatePaymentId(1L, "PAY-1");
        verify(producer).publish(any());
    }

    @Test
    void createReservation_datesInvalides_leveException() {
        when(userClient.getUserById(5L)).thenReturn(user());
        when(propertyClient.getProperty(10L)).thenReturn(property());
        when(repository.hasConflict(eq(10L), any(), any())).thenReturn(false);

        ReservationRequest request = request();
        request.setEndDate(request.getStartDate());

        assertThrows(BusinessException.class, () -> reservationService.createReservation(5L, request));
    }

    @Test
    void confirmReservation_introuvable_leveException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> reservationService.confirmReservation(1L));
    }

    @Test
    void confirmReservation_passeStatutConfirmeEtLibereBien() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setPropertyId(10L);
        reservation.setStatus(ReservationStatus.EN_ATTENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(reservation));
        when(repository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(propertyClient).changerStatut(eq(10L), any());

        ReservationResponse response = reservationService.confirmReservation(1L);

        assertEquals(ReservationStatus.CONFIRME.name(), response.getStatus());
        verify(propertyClient).changerStatut(eq(10L), any());
        verify(producer).publish(any());
    }

    @Test
    void cancelReservation_passeStatutAnnuleEtRemetBienDisponible() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setPropertyId(10L);
        reservation.setStatus(ReservationStatus.EN_ATTENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(reservation));
        when(repository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = reservationService.cancelReservation(1L);

        assertEquals(ReservationStatus.ANNULE.name(), response.getStatus());
        verify(propertyClient).changerStatut(eq(10L), any());
        verify(producer).publish(any());
    }

    @Test
    void expireOldReservations_expireUniquementCellesDe24hOuPlus() {
        Reservation ancienne = new Reservation();
        ancienne.setId(1L);
        ancienne.setStatus(ReservationStatus.EN_ATTENTE);
        ancienne.setCreatedAt(LocalDateTime.now().minusHours(25));

        Reservation recente = new Reservation();
        recente.setId(2L);
        recente.setStatus(ReservationStatus.EN_ATTENTE);
        recente.setCreatedAt(LocalDateTime.now().minusHours(1));

        when(repository.findByStatus(ReservationStatus.EN_ATTENTE)).thenReturn(List.of(ancienne, recente));
        when(repository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        reservationService.expireOldReservations();

        assertEquals(ReservationStatus.EXPIRE, ancienne.getStatus());
        assertEquals(ReservationStatus.EN_ATTENTE, recente.getStatus());
        verify(repository, times(1)).save(any(Reservation.class));
        verify(producer, times(1)).publish(any());
    }

    @Test
    void getReservationsByUser_delegueAuRepositoryEtMappe() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUserId(5L);
        reservation.setStatus(ReservationStatus.EN_ATTENTE);
        when(repository.findByUserId(5L)).thenReturn(List.of(reservation));

        List<ReservationResponse> result = reservationService.getReservationsByUser(5L);

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getUserId());
    }
}
