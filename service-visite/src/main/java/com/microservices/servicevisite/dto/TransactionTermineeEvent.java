package com.microservices.servicevisite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Événement publié sur le topic Kafka "transaction.terminee" lorsqu'une visite
 * passe à TERMINE. Consommé par ms-avis pour créer une opportunité de notation.
 * Contrat de payload strict partagé avec reservation-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTermineeEvent {

    private Long transactionId;

    private Long clientId;

    private Long agentId;

    private Long bienId;

    private String typeTransaction; // RESERVATION | VISITE
}
