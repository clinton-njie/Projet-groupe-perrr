package com.microservices.servicevisite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTermineeEvent {
private Long transactionId;
private String clientId;
private String agentId;
private Long bienId;
private String typeTransaction;
}
