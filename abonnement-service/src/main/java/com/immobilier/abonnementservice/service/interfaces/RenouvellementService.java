package com.immobilier.abonnementservice.service.interfaces;

import com.immobilier.abonnementservice.dto.request.RenouvelerAbonnementRequest;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;

public interface RenouvellementService {

    AbonnementResponse renouveler(String token, RenouvelerAbonnementRequest request);

}