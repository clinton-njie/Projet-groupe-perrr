package com.microservices.paymentservice.entity;

import com.microservices.paymentservice.enums.NatureType;
import com.microservices.paymentservice.enums.OperateurMobileMoney;
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

    // id de la réservation ou de la visite concernée
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    private NatureType referenceType;

    private String nomUtilisateur;

    private String nomBien;

    private Double montant;

    @Enumerated(EnumType.STRING)
    private OperateurMobileMoney operateur;

    private String telephone;

    @Column(unique = true)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    private PaymentStatut statut;

    private LocalDateTime createdAt;
}
