package com.microservices.servicevisite.service;

import com.microservices.servicevisite.dto.DecisionAgentRequest;
import com.microservices.servicevisite.dto.PaymentCallbackRequest;
import com.microservices.servicevisite.dto.SlotResponse;
import com.microservices.servicevisite.dto.TransactionTermineeEvent;
import com.microservices.servicevisite.dto.VisitRequest;
import com.microservices.servicevisite.dto.VisitResponse;
import com.microservices.servicevisite.entity.AgentAvailability;
import com.microservices.servicevisite.entity.Visit;
import com.microservices.servicevisite.enums.DisponibiliteType;
import com.microservices.servicevisite.enums.JourSemaine;
import com.microservices.servicevisite.enums.VisitStatut;
import com.microservices.servicevisite.feign.BienResponse;
import com.microservices.servicevisite.feign.BienServiceClient;
import com.microservices.servicevisite.feign.PaymentRequest;
import com.microservices.servicevisite.feign.PaymentServiceClient;
import com.microservices.servicevisite.kafka.TransactionEventProducer;
import com.microservices.servicevisite.repository.AgentAvailabilityRepository;
import com.microservices.servicevisite.repository.VisitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

@Mock
private VisitRepository visitRepository;

@Mock
private AgentAvailabilityRepository availabilityRepository;

@Mock
private BienServiceClient bienServiceClient;

@Mock
private PaymentServiceClient paymentServiceClient;

@Mock
private TransactionEventProducer transactionEventProducer;

@InjectMocks
private VisitService visitService;

@Test
void creerVisite_creeLaVisiteEnAttentePaiementEtInitieLePaiement() {
VisitRequest request = new VisitRequest();
request.setBienId(1L);
request.setClientEmail("client@test.com");
request.setDateHeureDebut(LocalDateTime.of(2026, 7, 20, 10, 0));
request.setDateHeureFin(LocalDateTime.of(2026, 7, 20, 11, 0));

BienResponse bien = new BienResponse();
bien.setId(1L);
bien.setAgentEmail("agent@test.com");
when(bienServiceClient.getBienById(1L)).thenReturn(bien);

when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> {
Visit v = invocation.getArgument(0);
v.setId(10L);
return v;
});

VisitResponse response = visitService.creerVisite(request);

assertEquals(10L, response.getId());
assertEquals(VisitStatut.EN_ATTENTE_PAIEMENT, response.getStatut());
assertEquals("agent@test.com", response.getAgentEmail());

ArgumentCaptor<PaymentRequest> paymentCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
verify(paymentServiceClient).initierPaiement(paymentCaptor.capture());
assertEquals(10L, paymentCaptor.getValue().getVisitId());
assertEquals("client@test.com", paymentCaptor.getValue().getClientId());
}

@Test
void confirmerPaiement_passeEnAttenteAgentSiPaiementReussi() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.EN_ATTENTE_PAIEMENT).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

PaymentCallbackRequest callback = new PaymentCallbackRequest();
callback.setStatut("REUSSI");
callback.setPaymentId("pay_1");

VisitResponse response = visitService.confirmerPaiement(1L, callback);

assertEquals(VisitStatut.EN_ATTENTE_AGENT, response.getStatut());
assertEquals("pay_1", response.getPaymentId());
}

@Test
void confirmerPaiement_passeEnAnnuleeSiPaiementEchoue() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.EN_ATTENTE_PAIEMENT).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

PaymentCallbackRequest callback = new PaymentCallbackRequest();
callback.setStatut("ECHEC");

VisitResponse response = visitService.confirmerPaiement(1L, callback);

assertEquals(VisitStatut.ANNULEE, response.getStatut());
}

@Test
void confirmerPaiement_leveUneExceptionSiVisiteIntrouvable() {
when(visitRepository.findById(99L)).thenReturn(Optional.empty());

assertThrows(RuntimeException.class,
() -> visitService.confirmerPaiement(99L, new PaymentCallbackRequest()));
}

@Test
void decisionAgent_confirmeLaVisiteEtPublieUnEvenementKafkaSiAccepte() {
Visit visit = Visit.builder()
.id(1L)
.bienId(5L)
.clientEmail("client@test.com")
.agentEmail("agent@test.com")
.statut(VisitStatut.EN_ATTENTE_AGENT)
.build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

DecisionAgentRequest request = new DecisionAgentRequest();
request.setDecision("ACCEPTER");

VisitResponse response = visitService.decisionAgent(1L, request);

assertEquals(VisitStatut.CONFIRMEE, response.getStatut());

ArgumentCaptor<TransactionTermineeEvent> eventCaptor =
ArgumentCaptor.forClass(TransactionTermineeEvent.class);
verify(transactionEventProducer).publierTransactionTerminee(eventCaptor.capture());

TransactionTermineeEvent event = eventCaptor.getValue();
assertEquals(1L, event.getTransactionId());
assertEquals("client@test.com", event.getClientId());
assertEquals("agent@test.com", event.getAgentId());
assertEquals(5L, event.getBienId());
assertEquals("VISITE", event.getTypeTransaction());
}

@Test
void decisionAgent_refuseLaVisiteEtNePublieAucunEvenementKafkaSiRefuse() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.EN_ATTENTE_AGENT).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

DecisionAgentRequest request = new DecisionAgentRequest();
request.setDecision("REFUSER");

VisitResponse response = visitService.decisionAgent(1L, request);

assertEquals(VisitStatut.REFUSEE, response.getStatut());
verifyNoInteractions(transactionEventProducer);
}

@Test
void decisionAgent_leveUneExceptionSiVisiteIntrouvable() {
when(visitRepository.findById(99L)).thenReturn(Optional.empty());

assertThrows(RuntimeException.class,
() -> visitService.decisionAgent(99L, new DecisionAgentRequest()));
}

@Test
void getAvailableSlots_genereLesCreneauxLibresSurUneJournee() {
LocalDate lundi = LocalDate.of(2026, 7, 20);

BienResponse bien = new BienResponse();
bien.setAgentEmail("agent@test.com");
when(bienServiceClient.getBienById(1L)).thenReturn(bien);

AgentAvailability dispo = AgentAvailability.builder()
.agentEmail("agent@test.com")
.jourSemaine(JourSemaine.LUNDI)
.heureDebut(LocalTime.of(9, 0))
.heureFin(LocalTime.of(11, 0))
.type(DisponibiliteType.RECURRENT)
.build();

when(availabilityRepository.findByAgentEmailAndJourSemaineAndType(
"agent@test.com", JourSemaine.LUNDI, DisponibiliteType.RECURRENT))
.thenReturn(List.of(dispo));

when(visitRepository.findByAgentEmailAndStatutNotIn(anyString(), anyList()))
.thenReturn(List.of());

List<SlotResponse> slots = visitService.getAvailableSlots(1L, lundi);

assertEquals(2, slots.size());
}
}
