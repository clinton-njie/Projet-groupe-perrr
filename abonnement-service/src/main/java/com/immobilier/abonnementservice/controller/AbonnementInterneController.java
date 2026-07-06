package com.immobilier.abonnementservice.controller;

import com.immobilier.abonnementservice.config.KafkaConfig;
import com.immobilier.abonnementservice.dto.event.PaiementTraiteEvent;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.dto.response.StatutAbonnementResponse;
import com.immobilier.abonnementservice.service.interfaces.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Endpoints internes (autres microservices) : sans JWT utilisateur (permitAll).
@RestController
@RequestMapping("/api/interne/abonnements")
@RequiredArgsConstructor
public class AbonnementInterneController {

    private final ConsultationService consultationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/utilisateur/{userId}/statut")
    public ResponseEntity<StatutAbonnementResponse> statut(@PathVariable Long userId) {
        return ResponseEntity.ok(consultationService.statutUtilisateur(userId));
    }

    @GetMapping("/utilisateur/{userId}/actif")
    public ResponseEntity<AbonnementResponse> actif(@PathVariable Long userId) {
        return ResponseEntity.ok(consultationService.abonnementActifUtilisateur(userId));
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<AbonnementResponse> parRequestId(@PathVariable String requestId) {
        return ResponseEntity.ok(consultationService.parRequestId(requestId));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(consultationService.count());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("service", "abonnement-service", "status", "UP"));
    }

    // SIMULATION : injecte un PaiementTraiteEvent sur le topic paiement-traite pour valider la
    // chaîne asynchrone sans dépendre de payment-service. À retirer en production.
    @PostMapping("/simuler-paiement")
    public ResponseEntity<String> simulerPaiement(@RequestBody PaiementTraiteEvent event) {
        kafkaTemplate.send(KafkaConfig.TOPIC_PAIEMENT_TRAITE, event.getRequestId(), event);
        return ResponseEntity.accepted().body("PaiementTraiteEvent publié (traitement asynchrone)");
    }
}
