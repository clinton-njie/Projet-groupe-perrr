package com.microservices.servicevisite.entity;

import com.microservices.servicevisite.enums.DisponibiliteType;
import com.microservices.servicevisite.enums.JourSemaine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "agent_availability")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentAvailability {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String agentEmail;

@Enumerated(EnumType.STRING)
private JourSemaine jourSemaine;

private LocalDate dateSpecifique;

private LocalTime heureDebut;

private LocalTime heureFin;

@Enumerated(EnumType.STRING)
private DisponibiliteType type;
}