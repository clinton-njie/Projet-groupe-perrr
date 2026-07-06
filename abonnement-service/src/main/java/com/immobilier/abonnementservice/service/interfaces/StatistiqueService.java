package com.immobilier.abonnementservice.service.interfaces;

import com.immobilier.abonnementservice.dto.response.StatistiqueAbonnementResponse;

import java.time.LocalDate;

public interface StatistiqueService {

    StatistiqueAbonnementResponse statistiquesGlobales();

    StatistiqueAbonnementResponse statistiquesParPeriode(LocalDate debut, LocalDate fin);
}
