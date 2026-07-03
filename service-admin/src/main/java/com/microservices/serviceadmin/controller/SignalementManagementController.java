package com.microservices.serviceadmin.controller;

import com.microservices.serviceadmin.dto.UpdateSignalementStatutRequest;
import com.microservices.serviceadmin.feign.SignalementResponse;
import com.microservices.serviceadmin.service.SignalementManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/signalements")
@RequiredArgsConstructor
public class SignalementManagementController {

private final SignalementManagementService signalementManagementService;

@GetMapping
public ResponseEntity<List<SignalementResponse>> listerSignalements() {
return ResponseEntity.ok(signalementManagementService.listerSignalements());
}

@GetMapping("/{id}")
public ResponseEntity<SignalementResponse> getSignalement(@PathVariable Long id) {
return ResponseEntity.ok(signalementManagementService.getSignalement(id));
}

@PutMapping("/{id}/statut")
public ResponseEntity<SignalementResponse> changerStatut(
@PathVariable Long id,
@Valid @RequestBody UpdateSignalementStatutRequest request) {
return ResponseEntity.ok(signalementManagementService.changerStatut(id, request.getStatut()));
}

// "Gestion objet signalement" : liste des types/objets de signalement possibles.
@GetMapping("/objets")
public ResponseEntity<List<String>> listerObjetsSignalement() {
return ResponseEntity.ok(signalementManagementService.listerObjetsSignalement());
}
}
