package com.microservices.paymentservice.service;

import com.microservices.paymentservice.dto.NotificationRequest;
import com.microservices.paymentservice.dto.PaymentRequest;
import com.microservices.paymentservice.dto.PaymentResponse;
import com.microservices.paymentservice.dto.ReservationCallbackRequest;
import com.microservices.paymentservice.dto.VisitConfirmationRequest;
import com.microservices.paymentservice.entity.Payment;
import com.microservices.paymentservice.enums.NatureType;
import com.microservices.paymentservice.enums.OperateurMobileMoney;
import com.microservices.paymentservice.enums.PaymentStatut;
import com.microservices.paymentservice.feign.NotificationServiceClient;
import com.microservices.paymentservice.feign.ReservationServiceClient;
import com.microservices.paymentservice.feign.VisiteServiceClient;
import com.microservices.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicePaiementTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private VisiteServiceClient visiteServiceClient;

    @Mock
    private ReservationServiceClient reservationServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private ServicePaiement servicePaiement;

    private PaymentRequest reservationRequest() {
        return new PaymentRequest(10L, NatureType.RESERVATION, "Jean", "Les Bougainvilliers", 150000.0,
                OperateurMobileMoney.ORANGE, "677000000");
    }

    private PaymentRequest visiteRequest() {
        return new PaymentRequest(20L, NatureType.VISITE, "Jean", "Villa X", 10000.0,
                OperateurMobileMoney.MTN, "677000001");
    }

    private void stubSaveAssignsId(Long id) {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId(id);
            return p;
        });
    }

    @Test
    void initierPaiement_reservation_enregistreEnReussiEtConfirmeLaReservation() {
        stubSaveAssignsId(1L);

        PaymentResponse response = servicePaiement.initierPaiement(reservationRequest());

        assertEquals("REUSSI", response.getStatut());
        assertNotNull(response.getPaymentId());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertEquals(10L, saved.getReferenceId());
        assertEquals(NatureType.RESERVATION, saved.getReferenceType());
        assertEquals(PaymentStatut.REUSSI, saved.getStatut());

        ArgumentCaptor<ReservationCallbackRequest> callbackCaptor = ArgumentCaptor.forClass(ReservationCallbackRequest.class);
        verify(reservationServiceClient).confirmer(eq(10L), callbackCaptor.capture());
        assertEquals("REUSSI", callbackCaptor.getValue().getStatut());
    }

    @Test
    void initierPaiement_visite_enregistreEnAutoriseEtConfirmeLaVisite() {
        stubSaveAssignsId(2L);

        PaymentResponse response = servicePaiement.initierPaiement(visiteRequest());

        assertEquals("AUTORISE", response.getStatut());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals(PaymentStatut.AUTORISE, paymentCaptor.getValue().getStatut());

        ArgumentCaptor<VisitConfirmationRequest> confirmationCaptor = ArgumentCaptor.forClass(VisitConfirmationRequest.class);
        verify(visiteServiceClient).confirmer(eq(20L), confirmationCaptor.capture());
        assertEquals("AUTORISE", confirmationCaptor.getValue().getStatut());
    }

    @Test
    void initierPaiement_notifieLeServiceNotification() {
        stubSaveAssignsId(3L);

        servicePaiement.initierPaiement(reservationRequest());

        ArgumentCaptor<NotificationRequest> notificationCaptor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationServiceClient).notifierPaiement(notificationCaptor.capture());
        assertEquals("Jean", notificationCaptor.getValue().getNomUtilisateur());
        assertEquals("SUCCESS", notificationCaptor.getValue().getStatut());
    }

    @Test
    void capturer_transfereLesFondsRetenusALAgent() {
        Payment payment = Payment.builder()
                .id(5L)
                .referenceId(20L)
                .referenceType(NatureType.VISITE)
                .nomUtilisateur("Jean")
                .nomBien("Villa X")
                .montant(10000.0)
                .statut(PaymentStatut.AUTORISE)
                .build();
        when(paymentRepository.findById(5L)).thenReturn(java.util.Optional.of(payment));
        lenient().when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = servicePaiement.capturer(5L);

        assertEquals("CAPTURE", response.getStatut());
        assertEquals(PaymentStatut.CAPTURE, payment.getStatut());
    }

    @Test
    void rembourser_rendLesFondsAuClient() {
        Payment payment = Payment.builder()
                .id(6L)
                .referenceId(21L)
                .referenceType(NatureType.VISITE)
                .nomUtilisateur("Marie")
                .nomBien("Cité Y")
                .montant(3000.0)
                .statut(PaymentStatut.AUTORISE)
                .build();
        when(paymentRepository.findById(6L)).thenReturn(java.util.Optional.of(payment));
        lenient().when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = servicePaiement.rembourser(6L);

        assertEquals("REMBOURSE", response.getStatut());
        assertEquals(PaymentStatut.REMBOURSE, payment.getStatut());
    }

    @Test
    void capturer_refuseSiLePaiementNEstPasAutorise() {
        Payment payment = Payment.builder()
                .id(7L)
                .statut(PaymentStatut.CAPTURE)
                .build();
        when(paymentRepository.findById(7L)).thenReturn(java.util.Optional.of(payment));

        try {
            servicePaiement.capturer(7L);
            throw new AssertionError("Une exception était attendue");
        } catch (IllegalStateException expected) {
            // ok
        }
    }
}
