package com.microservices.serviceadmin.service;

import com.microservices.serviceadmin.dto.AcceptInviteRequest;
import com.microservices.serviceadmin.dto.InviteAdminRequest;
import com.microservices.serviceadmin.dto.InviteResponse;
import com.microservices.serviceadmin.dto.InviteValidationResponse;
import com.microservices.serviceadmin.entity.AdminInvite;
import com.microservices.serviceadmin.feign.AuthServiceClient;
import com.microservices.serviceadmin.feign.RegisterAdminRequest;
import com.microservices.serviceadmin.repository.AdminInviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminInviteService {

private final AdminInviteRepository adminInviteRepository;
private final AuthServiceClient authServiceClient;

@Value("${app.invite-link-base-url}")
private String inviteLinkBaseUrl;

@Transactional
public InviteResponse inviterAdmin(InviteAdminRequest request) {
AdminInvite invite = AdminInvite.builder()
.token(UUID.randomUUID().toString())
.email(request.getEmail().toLowerCase().trim())
.role("ROLE_ADMIN")
.expiresAt(LocalDateTime.now().plusHours(48))
.createdAt(LocalDateTime.now())
.build();

adminInviteRepository.save(invite);

String inviteLink = inviteLinkBaseUrl + "?token=" + invite.getToken();

// Envoi d'email stubé : aucune configuration SMTP n'existe encore dans le projet.
// Le lien est loggé ici en attendant un vrai envoi.
System.out.println("[STUB EMAIL] Invitation admin envoyée à " + invite.getEmail() + " : " + inviteLink);

return new InviteResponse("Invitation envoyée à " + invite.getEmail(), inviteLink);
}

public InviteValidationResponse validerToken(String token) {
return adminInviteRepository.findByToken(token)
.filter(invite -> !invite.getUsed() && invite.getExpiresAt().isAfter(LocalDateTime.now()))
.map(invite -> new InviteValidationResponse(true, invite.getEmail()))
.orElse(new InviteValidationResponse(false, null));
}

@Transactional
public void accepterInvitation(String token, AcceptInviteRequest request) {
AdminInvite invite = adminInviteRepository.findByToken(token)
.orElseThrow(() -> new RuntimeException("Invitation introuvable"));

if (invite.getUsed()) {
throw new RuntimeException("Cette invitation a déjà été utilisée");
}
if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
throw new RuntimeException("Cette invitation a expiré");
}

authServiceClient.registerAdmin(new RegisterAdminRequest(
invite.getEmail(),
request.getPassword(),
request.getFirstname(),
request.getLastname(),
invite.getRole()
));

invite.setUsed(true);
adminInviteRepository.save(invite);
}
}
