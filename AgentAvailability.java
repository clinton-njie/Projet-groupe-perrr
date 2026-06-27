package com.immobilier.visitservice.entity;

import com.immobilier.visitservice.enums.AvailabilityType;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "agent_availabilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String agentEmail;

    // Pour les disponibilités récurrentes (par jour de la semaine)
    @Enumerated(EnumType.STRING)
    private DayOfWeek jourSemaine;

    // Pour les exceptions ponctuelles (date précise bloquée)
    private LocalDate dateSpecifique;

    @Column(nullable = false)
    private LocalTime heureDebut;

    @Column(nullable = false)
    private LocalTime heureFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityType type;
}
