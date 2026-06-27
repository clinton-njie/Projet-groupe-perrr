package com.microservices.servicevisite.repository;

import com.microservices.servicevisite.entity.AgentAvailability;
import com.microservices.servicevisite.enums.DisponibiliteType;
import com.microservices.servicevisite.enums.JourSemaine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AgentAvailabilityRepository
extends JpaRepository<AgentAvailability, Long> {

List<AgentAvailability> findByAgentEmailAndJourSemaineAndType(
String agentEmail,
JourSemaine jourSemaine,
DisponibiliteType type
);

List<AgentAvailability> findByAgentEmailAndDateSpecifiqueAndType(
String agentEmail,
LocalDate dateSpecifique,
DisponibiliteType type
);
}
