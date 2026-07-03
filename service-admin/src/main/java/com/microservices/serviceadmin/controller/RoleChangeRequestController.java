package com.microservices.serviceadmin.controller;

import com.microservices.serviceadmin.dto.CreateRoleRequestDto;
import com.microservices.serviceadmin.dto.ProcessRoleRequestDto;
import com.microservices.serviceadmin.dto.RoleRequestResponse;
import com.microservices.serviceadmin.service.RoleChangeRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoleChangeRequestController {

private final RoleChangeRequestService roleChangeRequestService;

// Appelé par service-user quand un client soumet une demande (pas de JWT admin dans ce contexte)
@PostMapping("/internal/role-requests")
public ResponseEntity<RoleRequestResponse> creerDemande(@Valid @RequestBody CreateRoleRequestDto request) {
return ResponseEntity.status(HttpStatus.CREATED).body(roleChangeRequestService.creerDemande(request));
}

@GetMapping("/admin/role-requests")
public ResponseEntity<List<RoleRequestResponse>> listerDemandes() {
return ResponseEntity.ok(roleChangeRequestService.listerDemandes());
}

@PutMapping("/admin/role-requests/{id}/process")
public ResponseEntity<RoleRequestResponse> traiterDemande(
@PathVariable Long id,
@Valid @RequestBody ProcessRoleRequestDto request) {
return ResponseEntity.ok(roleChangeRequestService.traiterDemande(id, request));
}
}
