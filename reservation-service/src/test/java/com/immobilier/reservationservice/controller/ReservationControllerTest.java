package com.immobilier.reservationservice.controller;

import com.immobilier.reservationservice.client.UserClient;
import com.immobilier.reservationservice.dto.ReservationCallbackRequest;
import com.immobilier.reservationservice.dto.ReservationResponse;
import com.immobilier.reservationservice.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    @Mock
    private ReservationService service;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private ReservationController controller;

    @Test
    void confirmer_statutReussi_confirmeLaReservation() {
        when(service.confirmReservation(1L)).thenReturn(new ReservationResponse());

        controller.confirmer(1L, new ReservationCallbackRequest("REUSSI", "PAY-1"));

        verify(service).confirmReservation(1L);
        verify(service, never()).cancelReservation(1L);
    }

    @Test
    void confirmer_bodyNull_traiteCommeUnSucces() {
        when(service.confirmReservation(1L)).thenReturn(new ReservationResponse());

        controller.confirmer(1L, null);

        verify(service).confirmReservation(1L);
        verify(service, never()).cancelReservation(1L);
    }

    @Test
    void confirmer_statutEchec_annuleLaReservation() {
        when(service.cancelReservation(1L)).thenReturn(new ReservationResponse());

        controller.confirmer(1L, new ReservationCallbackRequest("ECHEC", "PAY-1"));

        verify(service).cancelReservation(1L);
        verify(service, never()).confirmReservation(1L);
    }
}
