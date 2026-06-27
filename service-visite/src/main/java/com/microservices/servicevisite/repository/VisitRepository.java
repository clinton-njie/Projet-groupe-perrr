package com.microservices.servicevisite.repository;

import com.microservices.servicevisite.entity.Visit;
import com.microservices.servicevisite.enums.VisitStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

List<Visit> findByAgentEmailAndStatutNotIn(
String agentEmail,
List<VisitStatut> statuts
);

List<Visit> findByStatutAndCreatedAtBefore(
VisitStatut statut,
LocalDateTime dateTime
);
}
