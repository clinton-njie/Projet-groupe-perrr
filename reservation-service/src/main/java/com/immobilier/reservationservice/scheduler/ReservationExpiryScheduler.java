package com.immobilier.reservationservice.scheduler;

import com.immobilier.reservationservice.service.ReservationService;

import lombok.RequiredArgsConstructor;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationExpiryScheduler {
    //Permet de vérifier toutes les heures si des réservations sont expirées et de les mettre à jour en conséquence

    private final ReservationService service;

    @Scheduled(fixedRate = 3600000) //Date en millisecondes (ici toutes les heures)
    public void expireReservations() {

        service.expireOldReservations();
    }
}