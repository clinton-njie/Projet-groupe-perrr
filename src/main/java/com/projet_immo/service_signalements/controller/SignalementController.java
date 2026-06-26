package com.projet_immo.service_signalements.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projet_immo.service_signalements.dto.GetSignalementResponse;
import com.projet_immo.service_signalements.dto.SignalementRequest;
import com.projet_immo.service_signalements.dto.SignalementResponse;
import com.projet_immo.service_signalements.dto.StatutRequest;
import com.projet_immo.service_signalements.service.SignalementService;

@RestController
@RequestMapping("/signalements")
public class SignalementController {
    
    @Autowired
    private SignalementService signalementService;

     // Créer un signalement
    @PostMapping
    public ResponseEntity<SignalementResponse> addSignalement(
            @RequestBody SignalementRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {

        SignalementResponse response = signalementService.addSignalement(request, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Récupérer un signalement par son id
    @GetMapping("/{id}")
    public ResponseEntity<GetSignalementResponse> getSignalement(@PathVariable Long id) {
        return ResponseEntity.ok(signalementService.getSignalement(id));
    }

    // Liste complète pour l'admin
    @GetMapping
    public ResponseEntity<List<GetSignalementResponse>> getAllSignalements() {
        return ResponseEntity.ok(signalementService.getAllSignalements());
    }

    @PutMapping("/{id}/statut")
public ResponseEntity<GetSignalementResponse> updateStatut(
        @PathVariable Long id,
        @RequestBody StatutRequest statutRequest) {

    return ResponseEntity.ok(signalementService.updateStatut(id, statutRequest.getStatut()));
}
}
