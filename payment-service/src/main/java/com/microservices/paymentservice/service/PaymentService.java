package com.microservices.paymentservice.service;

import com.microservices.paymentservice.dto.PaymentRequest;
import com.microservices.paymentservice.dto.PaymentResponse;
import com.microservices.paymentservice.dto.VisitConfirmationRequest;
import com.microservices.paymentservice.entity.Payment;
import com.microservices.paymentservice.enums.PaymentStatut;
import com.microservices.paymentservice.feign.VisiteServiceClient;
import com.microservices.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

private final PaymentRepository paymentRepository;
private final VisiteServiceClient visiteServiceClient;

// Aucune vraie passerelle de paiement branchée : le paiement est toujours simulé réussi.
@Transactional
public PaymentResponse initierPaiement(PaymentRequest request) {
Payment payment = Payment.builder()
.visitId(request.getVisitId())
.montant(request.getMontant())
.clientId(request.getClientId())
.paymentReference(UUID.randomUUID().toString())
.statut(PaymentStatut.REUSSI)
.createdAt(LocalDateTime.now())
.build();

payment = paymentRepository.save(payment);

visiteServiceClient.confirmerPaiement(
request.getVisitId(),
new VisitConfirmationRequest(payment.getStatut().name(), payment.getPaymentReference())
);

return new PaymentResponse(payment.getPaymentReference(), payment.getStatut().name());
}
}
