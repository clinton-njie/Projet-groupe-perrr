package com.microservices.servicevisite.controller;

import com.microservices.servicevisite.dto.*;
import com.microservices.servicevisite.exception.AnnulationImpossibleException;
import com.microservices.servicevisite.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VisitController {

private final VisitService visitService;

// Inchangé : sans rapport avec le paiement/l'escrow, hors périmètre de cette refonte
@GetMapping("/visits/available-slots")
public ResponseEntity<List<SlotResponse>> getAvailableSlots(
@RequestParam Long bienId,
@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
) {
return ResponseEntity.ok(visitService.getAvailableSlots(bienId, date));
}

@PostMapping("/api/visites")
public ResponseEntity<VisitResponse> creerVisite(
@RequestBody VisitRequest request
) {
return ResponseEntity.status(HttpStatus.CREATED)
.body(visitService.creerVisite(request));
}

@GetMapping("/api/visites/{id}")
public ResponseEntity<VisitResponse> getById(@PathVariable Long id) {
return ResponseEntity.ok(visitService.getById(id));
}

@GetMapping("/api/visites/user/{clientEmail}")
public ResponseEntity<List<VisitResponse>> getByUser(@PathVariable String clientEmail) {
return ResponseEntity.ok(visitService.getByUser(clientEmail));
}

@GetMapping("/api/visites/agent/{agentEmail}")
public ResponseEntity<List<VisitResponse>> getByAgent(@PathVariable String agentEmail) {
return ResponseEntity.ok(visitService.getByAgent(agentEmail));
}

// Vérifie qu'un utilisateur a eu une interaction (visite effective) avec un agent.
// Utilisé par service-signalements. Appel Feign interne (visite est en permitAll).
@GetMapping("/api/visites/verification")
public ResponseEntity<Boolean> existeInteraction(
@RequestParam Long utilisateurId,
@RequestParam Long agentId) {
return ResponseEntity.ok(visitService.existeInteraction(utilisateurId, agentId));
}

// Callback payment-service
@PutMapping("/api/visites/{id}/confirmer")
public ResponseEntity<VisitResponse> confirmer(
@PathVariable Long id,
@RequestBody PaymentCallbackRequest request
) {
return ResponseEntity.ok(visitService.confirmer(id, request));
}

@PutMapping("/api/visites/{id}/terminer")
public ResponseEntity<VisitResponse> terminer(@PathVariable Long id) {
return ResponseEntity.ok(visitService.terminer(id));
}

@PutMapping("/api/visites/{id}/non-presente")
public ResponseEntity<VisitResponse> nonPresente(@PathVariable Long id) {
return ResponseEntity.ok(visitService.nonPresente(id));
}

@PutMapping("/api/visites/{id}/annuler")
public ResponseEntity<VisitResponse> annuler(@PathVariable Long id) {
return ResponseEntity.ok(visitService.annuler(id));
}

@PutMapping("/api/visites/{id}/annuler-agent")
public ResponseEntity<VisitResponse> annulerParAgent(@PathVariable Long id) {
return ResponseEntity.ok(visitService.annulerParAgent(id));
}

@ExceptionHandler(AnnulationImpossibleException.class)
public ResponseEntity<Map<String, String>> handleAnnulationImpossible(AnnulationImpossibleException ex) {
return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
}
}
