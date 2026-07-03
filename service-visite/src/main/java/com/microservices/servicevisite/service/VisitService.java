package com.microservices.servicevisite.service;

import com.microservices.servicevisite.dto.*;
import com.microservices.servicevisite.entity.*;
import com.microservices.servicevisite.enums.*;
import com.microservices.servicevisite.feign.*;
import com.microservices.servicevisite.kafka.TransactionEventProducer;
import com.microservices.servicevisite.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VisitService {

private final VisitRepository visitRepository;
private final AgentAvailabilityRepository availabilityRepository;
private final BienServiceClient bienServiceClient;
private final PaymentServiceClient paymentServiceClient;
private final TransactionEventProducer transactionEventProducer;

private static final Map<DayOfWeek, JourSemaine> JOURS_SEMAINE = Map.of(
DayOfWeek.MONDAY, JourSemaine.LUNDI,
DayOfWeek.TUESDAY, JourSemaine.MARDI,
DayOfWeek.WEDNESDAY, JourSemaine.MERCREDI,
DayOfWeek.THURSDAY, JourSemaine.JEUDI,
DayOfWeek.FRIDAY, JourSemaine.VENDREDI,
DayOfWeek.SATURDAY, JourSemaine.SAMEDI,
DayOfWeek.SUNDAY, JourSemaine.DIMANCHE
);

// Récupérer les créneaux disponibles
public List<SlotResponse> getAvailableSlots(Long bienId, LocalDate date) {
BienResponse bien = bienServiceClient.getBienById(bienId);
String agentEmail = bien.getAgentEmail();

JourSemaine jour = JOURS_SEMAINE.get(date.getDayOfWeek());

List<AgentAvailability> disponibilites =
availabilityRepository.findByAgentEmailAndJourSemaineAndType(
agentEmail, jour, DisponibiliteType.RECURRENT
);

List<Visit> visitsExistantes = visitRepository
.findByAgentEmailAndStatutNotIn(
agentEmail,
List.of(VisitStatut.ANNULEE, VisitStatut.REFUSEE)
);

List<SlotResponse> slots = new ArrayList<>();
for (AgentAvailability dispo : disponibilites) {
LocalDateTime debut = LocalDateTime.of(date, dispo.getHeureDebut());
LocalDateTime fin = LocalDateTime.of(date, dispo.getHeureFin());

while (debut.plusHours(1).compareTo(fin) <= 0) {
LocalDateTime slotDebut = debut;
LocalDateTime slotFin = debut.plusHours(1);

boolean occupe = visitsExistantes.stream()
.anyMatch(v -> v.getDateHeureDebut().equals(slotDebut));

if (!occupe) {
slots.add(new SlotResponse(slotDebut, slotFin));
}
debut = debut.plusHours(1);
}
}
return slots;
}

// Créer une visite
// Pas de @Transactional ici : le paiement est initié en synchrone juste après l'enregistrement,
// et payment-service rappelle confirmation-paiement dans la foulée. Si cette méthode était
// transactionnelle, la visite ne serait pas encore committée quand ce rappel arrive
// (findById échouerait avec "Visite non trouvée").
public VisitResponse creerVisite(VisitRequest request) {
BienResponse bien = bienServiceClient.getBienById(request.getBienId());

Visit visit = Visit.builder()
.bienId(request.getBienId())
.clientEmail(request.getClientEmail())
.agentEmail(bien.getAgentEmail())
.dateHeureDebut(request.getDateHeureDebut())
.dateHeureFin(request.getDateHeureFin())
.statut(VisitStatut.EN_ATTENTE_PAIEMENT)
.createdAt(LocalDateTime.now())
.build();

visit = visitRepository.save(visit);

PaymentRequest paymentRequest = new PaymentRequest(
visit.getId(), 5000.0, visit.getClientEmail()
);
paymentServiceClient.initierPaiement(paymentRequest);

return mapToResponse(visit);
}

// Callback paiement
@Transactional
public VisitResponse confirmerPaiement(Long id, PaymentCallbackRequest request) {
Visit visit = visitRepository.findById(id)
.orElseThrow(() -> new RuntimeException("Visite non trouvée"));

if ("REUSSI".equals(request.getStatut())) {
visit.setStatut(VisitStatut.EN_ATTENTE_AGENT);
visit.setPaymentId(request.getPaymentId());
} else {
visit.setStatut(VisitStatut.ANNULEE);
}

return mapToResponse(visitRepository.save(visit));
}

// Décision agent
@Transactional
public VisitResponse decisionAgent(Long id, DecisionAgentRequest request) {
Visit visit = visitRepository.findById(id)
.orElseThrow(() -> new RuntimeException("Visite non trouvée"));

if ("ACCEPTER".equals(request.getDecision())) {
visit.setStatut(VisitStatut.CONFIRMEE);
visit = visitRepository.save(visit);

transactionEventProducer.publierTransactionTerminee(
TransactionTermineeEvent.builder()
.transactionId(visit.getId())
.clientId(visit.getClientEmail())
.agentId(visit.getAgentEmail())
.bienId(visit.getBienId())
.typeTransaction("VISITE")
.build()
);

return mapToResponse(visit);
} else {
visit.setStatut(VisitStatut.REFUSEE);
return mapToResponse(visitRepository.save(visit));
}
}

// Mapper entité → DTO
private VisitResponse mapToResponse(Visit visit) {
VisitResponse response = new VisitResponse();
response.setId(visit.getId());
response.setBienId(visit.getBienId());
response.setClientEmail(visit.getClientEmail());
response.setAgentEmail(visit.getAgentEmail());
response.setDateHeureDebut(visit.getDateHeureDebut());
response.setDateHeureFin(visit.getDateHeureFin());
response.setStatut(visit.getStatut());
response.setPaymentId(visit.getPaymentId());
response.setCreatedAt(visit.getCreatedAt());
return response;
}
}

