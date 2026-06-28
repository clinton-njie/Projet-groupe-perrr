package com.immobilier.abonnementservice.controller;

import com.immobilier.abonnementservice.dto.commun.ApiResponse;
import com.immobilier.abonnementservice.dto.request.CreerAbonnementRequest;
import com.immobilier.abonnementservice.dto.request.RenouvelerAbonnementRequest;
import com.immobilier.abonnementservice.dto.response.*;
import com.immobilier.abonnementservice.service.interfaces.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final SouscriptionService souscriptionService;
    private final RenouvellementService renouvellementService;
    private final ConsultationService consultationService;

    // =========================
    // SOUSCRIPTION
    // =========================
    @PostMapping("/souscrire")
    public ApiResponse<AbonnementResponse> souscrire(
            @RequestHeader("Authorization") String token,
            @RequestBody CreerAbonnementRequest request
    ) {

        return ApiResponse.<AbonnementResponse>builder()
                .success(true)
                .message("Abonnement créé avec succès")
                .timestamp(LocalDateTime.now())
                .data(souscriptionService.souscrire(token, request))
                .build();
    }

    // =========================
    // RENOUVELLEMENT
    // =========================
    @PostMapping("/renouveler")
    public ApiResponse<AbonnementResponse> renouveler(
            @RequestHeader("Authorization") String token,
            @RequestBody RenouvelerAbonnementRequest request
    ) {

        return ApiResponse.<AbonnementResponse>builder()
                .success(true)
                .message("Abonnement renouvelé avec succès")
                .timestamp(LocalDateTime.now())
                .data(renouvellementService.renouveler(token, request))
                .build();
    }

    // =========================
    // MON ABONNEMENT
    // =========================
    @GetMapping("/me")
    public ApiResponse<AbonnementResponse> monAbonnement(
            @RequestHeader("Authorization") String token
    ) {

        return ApiResponse.<AbonnementResponse>builder()
                .success(true)
                .message("Abonnement récupéré")
                .timestamp(LocalDateTime.now())
                .data(consultationService.monAbonnement(token))
                .build();
    }

    // =========================
    // MON STATUT
    // =========================
    @GetMapping("/me/statut")
    public ApiResponse<StatutAbonnementResponse> monStatut(
            @RequestHeader("Authorization") String token
    ) {

        return ApiResponse.<StatutAbonnementResponse>builder()
                .success(true)
                .message("Statut récupéré")
                .timestamp(LocalDateTime.now())
                .data(consultationService.monStatut(token))
                .build();
    }

    // =========================
    // HISTORIQUE
    // =========================
    @GetMapping("/me/historique")
    public ApiResponse<HistoriqueAbonnementResponse> historique(
            @RequestHeader("Authorization") String token
    ) {

        return ApiResponse.<HistoriqueAbonnementResponse>builder()
                .success(true)
                .message("Historique récupéré")
                .timestamp(LocalDateTime.now())
                .data(consultationService.monHistorique(token))
                .build();
    }
}