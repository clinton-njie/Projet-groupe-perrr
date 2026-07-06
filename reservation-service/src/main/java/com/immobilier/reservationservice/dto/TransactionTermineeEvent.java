package com.immobilier.reservationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Événement publié sur le topic Kafka "transaction.terminee" lorsqu'une réservation
 * passe à CONFIRME. Consommé par ms-avis pour créer une opportunité de notation.
 * Contrat de payload strict partagé avec ms-visite.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionTermineeEvent {

    private Long transactionId;

    private Long clientId;

    private Long agentId;

    private Long bienId;

    private String typeTransaction; // RESERVATION | VISITE
}
