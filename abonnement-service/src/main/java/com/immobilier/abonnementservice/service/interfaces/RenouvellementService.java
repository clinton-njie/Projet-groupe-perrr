package com.immobilier.abonnementservice.service.interfaces;

import com.immobilier.abonnementservice.dto.request.RenouvelerAbonnementRequest;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;

public interface RenouvellementService {

    // Renouvellement (agent) : interdit si un abonnement actif est encore valide
    AbonnementResponse renouveler(String authHeader, RenouvelerAbonnementRequest request);
}
