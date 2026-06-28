package com.immobilier.abonnementservice.service.interfaces;

import com.immobilier.abonnementservice.dto.response.*;

public interface ConsultationService {

    AbonnementResponse monAbonnement(String token);

    HistoriqueAbonnementResponse monHistorique(String token);

    StatutAbonnementResponse monStatut(String token);
}