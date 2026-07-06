package com.microservices.servicevisite.dto;

import com.microservices.servicevisite.enums.VisitStatut;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VisitResponse {
private Long id;
private Long bienId;
private String clientEmail;
private String agentEmail;
private LocalDateTime dateHeureDebut;
private LocalDateTime dateHeureFin;
private VisitStatut statut;
private String paymentId;
private LocalDateTime createdAt;
private String nomUtilisateur;
private String nomBien;
private boolean annulationVerrouillee;
}
