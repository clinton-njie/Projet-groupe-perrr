package com.plateforme.immo.avis.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunites_notation",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_opportunite_transaction_type",
                columnNames = {"transaction_id", "type_transaction"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpportuniteNotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Long agentId;

    @Column(nullable = false)
    private Long bienId;

    @Column(nullable = false)
    private Long transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransaction typeTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutOpportunite statut;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateExpiration;
}