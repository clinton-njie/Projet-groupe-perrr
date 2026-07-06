package com.microservices.notification.service;

import com.microservices.notification.dto.GenericNotificationRequest;
import com.microservices.notification.dto.NatureType;
import com.microservices.notification.dto.NotificationRequest;
import com.microservices.notification.dto.NotificationResponse;
import com.microservices.notification.dto.StatutPaiement;
import com.microservices.notification.entity.Notification;
import com.microservices.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationResponse creerNotification(NotificationRequest request) {
        String message = formatMessage(request);

        Notification notification = Notification.builder()
                .destinataire(request.getNomUtilisateur())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification envoyée à {} : {}", saved.getDestinataire(), saved.getMessage());

        return NotificationResponse.builder()
                .id(saved.getId())
                .destinataire(saved.getDestinataire())
                .message(saved.getMessage())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public NotificationResponse creerNotification(GenericNotificationRequest request) {
        Notification notification = Notification.builder()
                .destinataire(request.getDestinataire())
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification envoyée à {} : {}", saved.getDestinataire(), saved.getMessage());

        return NotificationResponse.builder()
                .id(saved.getId())
                .destinataire(saved.getDestinataire())
                .message(saved.getMessage())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private String formatMessage(NotificationRequest request) {
        String nom = request.getNomUtilisateur();
        String bien = request.getNomBien();
        String montant = request.getMontant() != null ? String.valueOf(request.getMontant().longValue()) : "";
        String typeOperation = request.getNatureType() == NatureType.RESERVATION ? "réservation" : "visite";

        if (request.getStatut() == StatutPaiement.SUCCESS) {
            return "Bonjour " + nom + ", votre " + typeOperation + " de l'appartement " + bien
                    + " a été confirmée, montant payé " + montant + " FCFA";
        }
        if (request.getStatut() == StatutPaiement.REFUNDED) {
            return "Bonjour " + nom + ", votre " + typeOperation + " de " + bien
                    + " a été annulée, vous avez été remboursé " + montant + " FCFA.";
        }
        return "Bonjour " + nom + ", votre paiement pour la " + typeOperation + " de l'appartement " + bien
                + " a échoué, veuillez réessayer.";
    }
}
