package com.microservices.servicevisite.dto;

import com.microservices.servicevisite.enums.OperateurMobileMoney;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VisitRequest {
private Long bienId;
private String clientEmail;
private LocalDateTime dateHeureDebut;
private LocalDateTime dateHeureFin;
private OperateurMobileMoney operateur;
private String telephone;
}
