package com.microservices.paymentservice.service;

import com.microservices.paymentservice.dto.*;
import com.microservices.paymentservice.entity.Payment;
import com.microservices.paymentservice.enums.NatureType;
import com.microservices.paymentservice.enums.PaymentStatut;
import com.microservices.paymentservice.feign.NotificationServiceClient;
import com.microservices.paymentservice.feign.ReservationServiceClient;
import com.microservices.paymentservice.feign.VisiteServiceClient;
import com.microservices.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServicePaiement {

    private final PaymentRepository paymentRepository;
    private final VisiteServiceClient visiteServiceClient;
    private final ReservationServiceClient reservationServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    // Aucune vraie passerelle de paiement branchée : le paiement est toujours simulé réussi.
    // RESERVATION : paiement direct (REUSSI/ECHEC). VISITE : mode escrow (AUTORISE = fonds retenus).
    @Transactional
    public PaymentResponse initierPaiement(PaymentRequest request) {
        boolean estVisite = request.getReferenceType() == NatureType.VISITE;
        PaymentStatut statut = estVisite ? PaymentStatut.AUTORISE : PaymentStatut.REUSSI;

        Payment payment = Payment.builder()
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .nomUtilisateur(request.getNomUtilisateur())
                .nomBien(request.getNomBien())
                .montant(request.getMontant())
                .operateur(request.getOperateur())
                .telephone(request.getTelephone())
                .paymentReference(UUID.randomUUID().toString())
                .statut(statut)
                .createdAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        if (estVisite) {
            visiteServiceClient.confirmer(payment.getReferenceId(),
                    new VisitConfirmationRequest("AUTORISE", payment.getId().toString()));
        } else {
            reservationServiceClient.confirmer(payment.getReferenceId(),
                    new ReservationCallbackRequest(statut.name(), payment.getId().toString()));
        }

        // Réservation (REUSSI) comme visite (AUTORISE = fonds débités et retenus en escrow) :
        // dans les deux cas l'argent a bien quitté le client -> notification de succès.
        notifier(payment, "SUCCESS");

        return new PaymentResponse(payment.getId().toString(), payment.getStatut().name());
    }

    // Transfert des fonds retenus à l'agent (visite terminée ou client absent)
    @Transactional
    public PaymentResponse capturer(Long paymentId) {
        Payment payment = getPaymentAutorise(paymentId);
        payment.setStatut(PaymentStatut.CAPTURE);
        paymentRepository.save(payment);

        notifier(payment, "SUCCESS");

        return new PaymentResponse(payment.getId().toString(), payment.getStatut().name());
    }

    // Remboursement au client (annulation client à plus de 24h du début, ou annulation par l'agent)
    @Transactional
    public PaymentResponse rembourser(Long paymentId) {
        Payment payment = getPaymentAutorise(paymentId);
        payment.setStatut(PaymentStatut.REMBOURSE);
        paymentRepository.save(payment);

        notifier(payment, "REFUNDED");

        return new PaymentResponse(payment.getId().toString(), payment.getStatut().name());
    }

    private Payment getPaymentAutorise(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("Paiement introuvable : " + paymentId));
        if (payment.getStatut() != PaymentStatut.AUTORISE) {
            throw new IllegalStateException("Le paiement " + paymentId + " n'est pas en attente de capture/remboursement (statut actuel : " + payment.getStatut() + ")");
        }
        return payment;
    }

    private void notifier(Payment payment, String statutNotification) {
        notificationServiceClient.notifierPaiement(new NotificationRequest(
                payment.getNomUtilisateur(),
                payment.getNomBien(),
                payment.getReferenceType().name(),
                statutNotification,
                payment.getMontant()
        ));
    }
}
