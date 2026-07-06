package com.immobilier.abonnementservice.controller;

import com.immobilier.abonnementservice.dto.response.AbonnementResponse;
import com.immobilier.abonnementservice.dto.response.HistoriqueAbonnementResponse;
import com.immobilier.abonnementservice.dto.response.StatistiqueAbonnementResponse;
import com.immobilier.abonnementservice.service.interfaces.ConsultationService;
import com.immobilier.abonnementservice.service.interfaces.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// Espace ADMIN : consultation globale + statistiques.
@RestController
@RequestMapping("/api/admin/abonnements")
@RequiredArgsConstructor
public class AdminAbonnementController {

    private final ConsultationService consultationService;
    private final StatistiqueService statistiqueService;

    @GetMapping
    public ResponseEntity<List<AbonnementResponse>> tous() {
        return ResponseEntity.ok(consultationService.tousLesAbonnements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AbonnementResponse> parId(@PathVariable Long id) {
        return ResponseEntity.ok(consultationService.parId(id));
    }

    @GetMapping("/utilisateur/{userId}/historique")
    public ResponseEntity<HistoriqueAbonnementResponse> historique(@PathVariable Long userId) {
        return ResponseEntity.ok(consultationService.historiqueUtilisateur(userId));
    }

    @GetMapping("/utilisateur/{userId}/actif")
    public ResponseEntity<AbonnementResponse> actif(@PathVariable Long userId) {
        return ResponseEntity.ok(consultationService.abonnementActifUtilisateur(userId));
    }

    @GetMapping("/statistiques")
    public ResponseEntity<StatistiqueAbonnementResponse> stats() {
        return ResponseEntity.ok(statistiqueService.statistiquesGlobales());
    }

    @GetMapping("/statistiques/periode")
    public ResponseEntity<StatistiqueAbonnementResponse> statsPeriode(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(statistiqueService.statistiquesParPeriode(debut, fin));
    }
}
