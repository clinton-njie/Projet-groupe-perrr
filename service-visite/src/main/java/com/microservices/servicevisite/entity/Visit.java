package com.microservices.servicevisite.entity;

import com.microservices.servicevisite.enums.OperateurMobileMoney;
import com.microservices.servicevisite.enums.VisitStatut;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
name = "visits",
uniqueConstraints = {
@UniqueConstraint(columnNames = {"agentEmail", "dateHeureDebut"})
}
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Visit {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private Long bienId;

private String clientEmail;

private String agentEmail;

private LocalDateTime dateHeureDebut;

private LocalDateTime dateHeureFin;

@Enumerated(EnumType.STRING)
private VisitStatut statut;

private String paymentId;

private LocalDateTime createdAt;

// snapshots utilisés pour le paiement/la notification
private String nomUtilisateur;

private String nomBien;

private String categorieBien;

@Enumerated(EnumType.STRING)
private OperateurMobileMoney operateur;

private String telephone;

// verrouillée par CancellationLockScheduler quand il reste <24h avant la visite
@Builder.Default
private boolean annulationVerrouillee = false;
}
