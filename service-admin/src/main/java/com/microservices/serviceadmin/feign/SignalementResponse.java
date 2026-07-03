package com.microservices.serviceadmin.feign;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SignalementResponse {
private Long id;
private String titre;
private String type;
private String description;
private LocalDate dateSignalement;
private String statut;
private String utilisateurEmail;
private String agentEmail;
}
