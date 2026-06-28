package com.immobilier.abonnementservice.service.interfaces;

import com.immobilier.abonnementservice.dto.request.CreerAbonnementRequest;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;

public interface SouscriptionService {

    AbonnementResponse souscrire(String token, CreerAbonnementRequest request);

}