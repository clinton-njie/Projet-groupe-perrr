package com.microservices.notification.service;

import com.microservices.notification.dto.NatureType;
import com.microservices.notification.dto.NotificationRequest;
import com.microservices.notification.dto.NotificationResponse;
import com.microservices.notification.dto.StatutPaiement;
import com.microservices.notification.entity.Notification;
import com.microservices.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationRequest request(NatureType nature, StatutPaiement statut) {
        return NotificationRequest.builder()
                .nomUtilisateur("Jean")
                .nomBien("Villa Bella")
                .natureType(nature)
                .statut(statut)
                .montant(150000.0)
                .build();
    }

    @Test
    void creerNotification_reservationReussie_formateMessageDeConfirmation() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(1L);
            return n;
        });

        NotificationResponse response = notificationService.creerNotification(request(NatureType.RESERVATION, StatutPaiement.SUCCESS));

        assertEquals("Jean", response.getDestinataire());
        assertTrue(response.getMessage().contains("réservation"));
        assertTrue(response.getMessage().contains("confirmée"));
        assertTrue(response.getMessage().contains("150000"));
    }

    @Test
    void creerNotification_visiteRembourse_formateMessageDeRemboursement() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(2L);
            return n;
        });

        NotificationResponse response = notificationService.creerNotification(request(NatureType.VISITE, StatutPaiement.REFUNDED));

        assertTrue(response.getMessage().contains("visite"));
        assertTrue(response.getMessage().contains("remboursé"));
    }

    @Test
    void creerNotification_paiementEchoue_formateMessageDechec() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.creerNotification(request(NatureType.RESERVATION, StatutPaiement.FAILED));

        assertTrue(response.getMessage().contains("a échoué"));
    }

    @Test
    void creerNotification_persisteViaLeRepository() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(3L);
            return n;
        });

        notificationService.creerNotification(request(NatureType.RESERVATION, StatutPaiement.SUCCESS));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals("Jean", captor.getValue().getDestinataire());
    }
}
