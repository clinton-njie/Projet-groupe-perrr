package com.immobilier.abonnementservice.controller;

import com.immobilier.abonnementservice.dto.request.CreerAbonnementRequest;
import com.immobilier.abonnementservice.dto.request.RenouvelerAbonnementRequest;
import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.dto.response.HistoriqueAbonnementResponse;
import com.immobilier.abonnementservice.dto.response.StatutAbonnementResponse;
import com.immobilier.abonnementservice.service.interfaces.ConsultationService;
import com.immobilier.abonnementservice.service.interfaces.RenouvellementService;
import com.immobilier.abonnementservice.service.interfaces.SouscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Espace AGENT : souscrire, renouveler, réessayer, consulter son abonnement.
@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final SouscriptionService souscriptionService;
    private final RenouvellementService renouvellementService;
    private final ConsultationService consultationService;

    @PostMapping
    public ResponseEntity<AbonnementResponse> souscrire(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreerAbonnementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(souscriptionService.souscrire(token, request));
    }

    @PostMapping("/renouveler")
    public ResponseEntity<AbonnementResponse> renouveler(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody RenouvelerAbonnementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(renouvellementService.renouveler(token, request));
    }

    @PostMapping("/{id}/reessayer-paiement")
    public ResponseEntity<AbonnementResponse> reessayer(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        return ResponseEntity.ok(souscriptionService.reessayerPaiement(token, id));
    }

    @GetMapping("/mon-statut")
    public ResponseEntity<StatutAbonnementResponse> monStatut(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(consultationService.monStatut(token));
    }

    @GetMapping("/mon-historique")
    public ResponseEntity<HistoriqueAbonnementResponse> monHistorique(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(consultationService.monHistorique(token));
    }

    @GetMapping("/mon-abonnement-actif")
    public ResponseEntity<AbonnementResponse> monActif(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(consultationService.monAbonnementActif(token));
    }
}
