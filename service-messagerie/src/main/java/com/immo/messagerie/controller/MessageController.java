package com.immo.messagerie.controller;

import com.immo.messagerie.dto.ConversationResponse;
import com.immo.messagerie.dto.MessageRequest;
import com.immo.messagerie.dto.MessageResponse;
import com.immo.messagerie.service.ConversationService;
import com.immo.messagerie.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final ConversationService conversationService;
    private final RestTemplate restTemplate;

    @Value("${services.user-service-url:http://localhost:8082}")
    private String userServiceUrl;

    // Récupérer authUserId depuis l'email via service-user
    private Long getAuthUserId(Authentication authentication) {
        String email = authentication.getName();
        String url = userServiceUrl + "/api/users/by-email/" + email;
        return restTemplate.getForObject(url, Long.class);
    }

    // Envoyer un message
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            Authentication authentication) {
        Long senderId = getAuthUserId(authentication);
        return ResponseEntity.ok(messageService.sendMessage(senderId, request));
    }

    // Lire une conversation
    @GetMapping("/conversation/{id}")
    public ResponseEntity<List<MessageResponse>> getConversationMessages(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getConversationMessages(id));
    }

    // Boîte de réception
    @GetMapping("/inbox")
    public ResponseEntity<List<ConversationResponse>> getInbox(Authentication authentication) {
        Long userId = getAuthUserId(authentication);
        return ResponseEntity.ok(conversationService.getInbox(userId));
    }

    // Marquer comme lu
    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.markAsRead(id));
    }

    // Supprimer un message
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id, Authentication authentication) {
        Long userId = getAuthUserId(authentication);
        return ResponseEntity.ok(messageService.deleteMessage(id, userId));
    }

    // Supprimer une conversation
    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<String> deleteConversation(@PathVariable Long id, Authentication authentication) {
        Long userId = getAuthUserId(authentication);
        return ResponseEntity.ok(conversationService.deleteConversation(id, userId));
    }
}
