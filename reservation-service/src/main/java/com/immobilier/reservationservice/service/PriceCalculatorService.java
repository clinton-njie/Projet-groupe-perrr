package com.immobilier.reservationservice.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class PriceCalculatorService {
    //Classe de service pour calculer le prix d'une réservation en fonction du prix mensuel et des dates de début et de fin de la réservation.

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
}