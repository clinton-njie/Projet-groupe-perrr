package com.microservices.servicevisite.controller;

import com.microservices.servicevisite.dto.*;
import com.microservices.servicevisite.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {

private final VisitService visitService;

@GetMapping("/available-slots")
public ResponseEntity<List<SlotResponse>> getAvailableSlots(
@RequestParam Long bienId,
@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
) {
return ResponseEntity.ok(visitService.getAvailableSlots(bienId, date));
}

@PostMapping
public ResponseEntity<VisitResponse> creerVisite(
@RequestBody VisitRequest request
) {
return ResponseEntity.status(HttpStatus.CREATED)
.body(visitService.creerVisite(request));
}

@PutMapping("/{id}/confirmation-paiement")
public ResponseEntity<VisitResponse> confirmerPaiement(
@PathVariable Long id,
@RequestBody PaymentCallbackRequest request
) {
return ResponseEntity.ok(visitService.confirmerPaiement(id, request));
}

@PutMapping("/{id}/decision-agent")
public ResponseEntity<VisitResponse> decisionAgent(
@PathVariable Long id,
@RequestBody DecisionAgentRequest request
) {
return ResponseEntity.ok(visitService.decisionAgent(id, request));
}
}

