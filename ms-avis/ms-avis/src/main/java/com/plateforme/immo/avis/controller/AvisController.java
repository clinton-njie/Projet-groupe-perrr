package com.plateforme.immo.avis.controller;

import com.plateforme.immo.avis.dto.*;
import com.plateforme.immo.avis.model.*;
import com.plateforme.immo.avis.service.AvisService;
import com.plateforme.immo.avis.service.OpportuniteService;
import com.plateforme.immo.avis.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/avis")
public class AvisController {

    @Autowired private AvisService avisService;
    @Autowired private OpportuniteService opportuniteService;
    @Autowired private JwtUtil jwtUtil;

    private String cleanToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token manquant ou invalide");
        }
        return header.replace("Bearer ", "");
    }

    @GetMapping("/opportunites/{clientId}")
    public ResponseEntity<List<OpportuniteNotation>> getOpportunites(@PathVariable Long clientId, @RequestHeader("Authorization") String authHeader) {
        Long tokenUserId = jwtUtil.extractUserId(cleanToken(authHeader));
        if (!tokenUserId.equals(clientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(opportuniteService.getEnAttente(clientId));
    }

    @PostMapping("/transaction")
    public ResponseEntity<Avis> soumettreAvisTransaction(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody AvisTransactionDTO dto) {
        String token = cleanToken(authHeader);
        Long clientId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);

        if (!"CLIENT".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(avisService.soumettreAvisTransaction(clientId, dto));
    }

    @PostMapping("/plateforme")
    public ResponseEntity<Avis> soumettreAvisPlateforme(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody AvisPlatformeDTO dto) {
        String token = cleanToken(authHeader);
        Long clientId = jwtUtil.extractUserId(token);
        return ResponseEntity.status(HttpStatus.CREATED).body(avisService.soumettreAvisPlateforme(clientId, dto));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<AvisResponseDTO> getAvisAgent(@PathVariable Long agentId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(avisService.getAvisAgent(agentId, PageRequest.of(page, size)));
    }

    @GetMapping("/bien/{bienId}")
    public ResponseEntity<AvisResponseDTO> getAvisBien(@PathVariable Long bienId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(avisService.getAvisBien(bienId, PageRequest.of(page, size)));
    }

    @GetMapping("/plateforme")
    public ResponseEntity<Page<Avis>> getAvisPlateforme(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(avisService.getAvisPlateforme(PageRequest.of(page, size)));
    }
}