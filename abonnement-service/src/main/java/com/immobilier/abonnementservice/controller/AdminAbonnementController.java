package com.immobilier.abonnementservice.controller;

import com.immobilier.abonnementservice.dto.commun.ApiResponse;
import com.immobilier.abonnementservice.dto.response.StatistiqueAbonnementResponse;
import com.immobilier.abonnementservice.service.interfaces.StatistiqueService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/abonnements")
@RequiredArgsConstructor
public class AdminAbonnementController {

    private final StatistiqueService statistiqueService;

    // =========================
    // STATISTIQUES GLOBALES
    // =========================
    @GetMapping("/statistiques")
    public ApiResponse<StatistiqueAbonnementResponse> statistiques() {

        return ApiResponse.<StatistiqueAbonnementResponse>builder()
                .success(true)
                .message("Statistiques globales des abonnements")
                .timestamp(LocalDateTime.now())
                .data(statistiqueService.statistiques())
                .build();
    }
}