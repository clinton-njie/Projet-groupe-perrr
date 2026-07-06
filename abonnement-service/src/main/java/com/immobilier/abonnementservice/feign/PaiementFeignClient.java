package com.immobilier.abonnementservice.feign;

import com.immobilier.abonnementservice.dto.request.PaiementRequest;
import com.immobilier.abonnementservice.dto.response.PaiementResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Jamais de paiement direct ici : on demande une prise en charge et on reçoit un accusé
// {paiementId, statut = EN_ATTENTE}. Le résultat réel arrive ensuite via Kafka (paiement-traite).
@FeignClient(name = "payment-service")
public interface PaiementFeignClient {

    @PostMapping("/api/paiements")
    PaiementResponse creerPaiement(@RequestBody PaiementRequest request);
}
