package com.microservices.notification.controller;

import com.microservices.notification.dto.GenericNotificationRequest;
import com.microservices.notification.dto.NotificationRequest;
import com.microservices.notification.dto.NotificationResponse;
import com.microservices.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // POST /api/notifications/paiement → appelé par payment-service après chaque paiement (interne, pas d'auth)
    @PostMapping("/paiement")
    public ResponseEntity<NotificationResponse> notifierPaiement(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.creerNotification(request));
    }

    // POST /api/notifications → notification générique {destinataire, message} (interne, ex. service-signalements)
    @PostMapping
    public ResponseEntity<NotificationResponse> notifier(@Valid @RequestBody GenericNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.creerNotification(request));
    }
}
