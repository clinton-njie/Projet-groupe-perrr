package com.microservices.serviceadmin.controller;

import com.microservices.serviceadmin.dto.AcceptInviteRequest;
import com.microservices.serviceadmin.dto.InviteAdminRequest;
import com.microservices.serviceadmin.dto.InviteResponse;
import com.microservices.serviceadmin.dto.InviteValidationResponse;
import com.microservices.serviceadmin.service.AdminInviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/invites")
@RequiredArgsConstructor
public class AdminInviteController {

private final AdminInviteService adminInviteService;

// Super admin uniquement (voir SecurityConfig)
@PostMapping
public ResponseEntity<InviteResponse> inviterAdmin(@Valid @RequestBody InviteAdminRequest request) {
return ResponseEntity.status(HttpStatus.CREATED).body(adminInviteService.inviterAdmin(request));
}

// Public : suivi du lien reçu par email, avant création de compte
@GetMapping("/{token}/validate")
public ResponseEntity<InviteValidationResponse> validerToken(@PathVariable String token) {
return ResponseEntity.ok(adminInviteService.validerToken(token));
}

// Public : création du compte admin depuis la page "créer compte admin"
@PostMapping("/{token}/accept")
public ResponseEntity<Void> accepterInvitation(
@PathVariable String token,
@Valid @RequestBody AcceptInviteRequest request) {
adminInviteService.accepterInvitation(token, request);
return ResponseEntity.ok().build();
}
}
