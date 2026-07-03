package com.microservices.paymentservice.entity;

import com.microservices.paymentservice.enums.PaymentStatut;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private Long visitId;

private Double montant;

private String clientId;

@Column(unique = true)
private String paymentReference;

@Enumerated(EnumType.STRING)
private PaymentStatut statut;

private LocalDateTime createdAt;
}
