package com.microservices.serviceadmin.service;

import com.microservices.serviceadmin.dto.CreateRoleRequestDto;
import com.microservices.serviceadmin.dto.ProcessRoleRequestDto;
import com.microservices.serviceadmin.dto.RoleRequestResponse;
import com.microservices.serviceadmin.entity.RoleChangeRequest;
import com.microservices.serviceadmin.enums.RoleRequestStatut;
import com.microservices.serviceadmin.feign.AuthServiceClient;
import com.microservices.serviceadmin.feign.RoleUpdateRequest;
import com.microservices.serviceadmin.repository.RoleChangeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleChangeRequestService {

private static final int MAX_DEMANDES = 3;

private final RoleChangeRequestRepository roleChangeRequestRepository;
private final AuthServiceClient authServiceClient;

@Transactional
public RoleRequestResponse creerDemande(CreateRoleRequestDto request) {
long nbDemandes = roleChangeRequestRepository.countByAuthUserId(request.getAuthUserId());
if (nbDemandes >= MAX_DEMANDES) {
throw new RuntimeException("Nombre maximum de demandes (3) atteint pour cet utilisateur");
}

RoleChangeRequest roleRequest = RoleChangeRequest.builder()
.authUserId(request.getAuthUserId())
.currentRole(request.getCurrentRole())
.requestedRole(request.getRequestedRole())
.justification(request.getJustification())
.statut(RoleRequestStatut.EN_ATTENTE)
.createdAt(LocalDateTime.now())
.build();

roleRequest = roleChangeRequestRepository.save(roleRequest);

return mapToResponse(roleRequest);
}

public List<RoleRequestResponse> listerDemandes() {
return roleChangeRequestRepository.findAll()
.stream()
.map(this::mapToResponse)
.toList();
}

@Transactional
public RoleRequestResponse traiterDemande(Long id, ProcessRoleRequestDto request) {
RoleChangeRequest roleRequest = roleChangeRequestRepository.findById(id)
.orElseThrow(() -> new RuntimeException("Demande introuvable"));

if (roleRequest.getStatut() != RoleRequestStatut.EN_ATTENTE) {
throw new RuntimeException("Cette demande a déjà été traitée");
}

if ("ACCEPTER".equals(request.getDecision())) {
roleRequest.setStatut(RoleRequestStatut.APPROUVEE);
roleRequest.setProcessedAt(LocalDateTime.now());

authServiceClient.updateRole(
roleRequest.getAuthUserId(),
new RoleUpdateRequest(roleRequest.getRequestedRole())
);

notifier(roleRequest.getAuthUserId(), "Votre demande pour devenir " + roleRequest.getRequestedRole() + " a été approuvée.");
} else {
roleRequest.setStatut(RoleRequestStatut.REFUSEE);
roleRequest.setRefusalReason(request.getRefusalReason());
roleRequest.setProcessedAt(LocalDateTime.now());

notifier(roleRequest.getAuthUserId(), "Votre demande a été rejetée. Motif : " + request.getRefusalReason());
}

roleChangeRequestRepository.save(roleRequest);

return mapToResponse(roleRequest);
}

// Notification stubée : aucun mécanisme d'envoi de message service-à-service
// (sans JWT utilisateur) n'existe encore vers service-messagerie.
private void notifier(Long authUserId, String message) {
System.out.println("[STUB NOTIFICATION] Utilisateur " + authUserId + " : " + message);
}

private RoleRequestResponse mapToResponse(RoleChangeRequest r) {
return RoleRequestResponse.builder()
.id(r.getId())
.authUserId(r.getAuthUserId())
.currentRole(r.getCurrentRole())
.requestedRole(r.getRequestedRole())
.justification(r.getJustification())
.statut(r.getStatut())
.createdAt(r.getCreatedAt())
.processedAt(r.getProcessedAt())
.refusalReason(r.getRefusalReason())
.build();
}
}
