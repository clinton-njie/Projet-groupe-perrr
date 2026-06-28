package com.immobilier.abonnementservice.utils;

import com.immobilier.abonnementservice.enums.TypeAbonnement;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PrixAbonnementUtils {

    public static final BigDecimal PRIX_MENSUEL = new BigDecimal("10000");
    public static final BigDecimal PRIX_ANNUEL = new BigDecimal("100000");

    private PrixAbonnementUtils() {}

    public static BigDecimal calculerPrix(TypeAbonnement type) {

        return switch (type) {

            case MENSUEL -> PRIX_MENSUEL;

            case ANNUEL -> PRIX_ANNUEL;

        };

    }

    public static LocalDate calculerDateFin(LocalDate debut, TypeAbonnement type) {

        return switch (type) {

            case MENSUEL -> debut.plusMonths(1);

            case ANNUEL -> debut.plusYears(1);

        };

    }
}