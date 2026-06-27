package com.microservices.servicevisite.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VisitRequest {
private Long bienId;
private String clientEmail;
private LocalDateTime dateHeureDebut;
private LocalDateTime dateHeureFin;
}
