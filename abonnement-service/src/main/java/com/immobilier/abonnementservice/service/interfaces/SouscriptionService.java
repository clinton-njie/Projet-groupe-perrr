package com.immobilier.abonnementservice.service.interfaces;

import com.immobilier.abonnementservice.dto.request.CreerAbonnementRequest;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;

public interface SouscriptionService {

    // Souscription (agent) : crée EN_ATTENTE_PAIEMENT + requestId + demande de paiement
    AbonnementResponse souscrire(String authHeader, CreerAbonnementRequest request);

    // Réessai d'un paiement échoué (agent propriétaire, statut ECHEC_PAIEMENT) : nouveau requestId
    AbonnementResponse reessayerPaiement(String authHeader, Long abonnementId);
}
