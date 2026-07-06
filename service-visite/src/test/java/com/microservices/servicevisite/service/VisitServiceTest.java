package com.microservices.servicevisite.service;

import com.microservices.servicevisite.dto.PaymentCallbackRequest;
import com.microservices.servicevisite.dto.SlotResponse;
import com.microservices.servicevisite.dto.TransactionTermineeEvent;
import com.microservices.servicevisite.dto.VisitRequest;
import com.microservices.servicevisite.dto.VisitResponse;
import com.microservices.servicevisite.entity.AgentAvailability;
import com.microservices.servicevisite.entity.Visit;
import com.microservices.servicevisite.enums.DisponibiliteType;
import com.microservices.servicevisite.enums.JourSemaine;
import com.microservices.servicevisite.enums.OperateurMobileMoney;
import com.microservices.servicevisite.enums.VisitStatut;
import com.microservices.servicevisite.exception.AnnulationImpossibleException;
import com.microservices.servicevisite.feign.BienResponse;
import com.microservices.servicevisite.feign.BienServiceClient;
import com.microservices.servicevisite.feign.PaymentRequest;
import com.microservices.servicevisite.feign.PaymentServiceClient;
import com.microservices.servicevisite.feign.PrixVisiteResponse;
import com.microservices.servicevisite.feign.UserServiceClient;
import com.microservices.servicevisite.feign.UserServiceResponse;
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
import static org.mockito.Mockito.lenient;
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
private UserServiceClient userServiceClient;

@Mock
private TransactionEventProducer transactionEventProducer;

@InjectMocks
private VisitService visitService;

private BienResponse bien(String categorie) {
BienResponse bien = new BienResponse();
bien.setId(1L);
bien.setNom("Villa Test");
bien.setAgentEmail("agent@test.com");
bien.setCategorie(categorie);
return bien;
}

@Test
void creerVisite_creeLaVisiteEnAttenteEtInitieLePaiementEscrow() {
VisitRequest request = new VisitRequest();
request.setBienId(1L);
request.setClientEmail("client@test.com");
request.setDateHeureDebut(LocalDateTime.of(2026, 7, 20, 10, 0));
request.setDateHeureFin(LocalDateTime.of(2026, 7, 20, 11, 0));
request.setOperateur(OperateurMobileMoney.ORANGE);
request.setTelephone("677000000");

when(bienServiceClient.getBienById(1L)).thenReturn(bien("VILLA"));

PrixVisiteResponse prix = new PrixVisiteResponse();
prix.setCategorie("VILLA");
prix.setPrixVisite(10000f);
when(bienServiceClient.getPrixVisite("VILLA")).thenReturn(prix);

when(userServiceClient.getAuthUserIdByEmail("client@test.com")).thenReturn(42L);
UserServiceResponse profil = new UserServiceResponse();
profil.setAuthUserId(42L);
profil.setFirstname("Jean");
profil.setLastname("Dupont");
when(userServiceClient.getProfile(42L)).thenReturn(profil);

when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> {
Visit v = invocation.getArgument(0);
v.setId(10L);
return v;
});

VisitResponse response = visitService.creerVisite(request);

assertEquals(10L, response.getId());
assertEquals(VisitStatut.EN_ATTENTE, response.getStatut());
assertEquals("agent@test.com", response.getAgentEmail());
assertEquals("Jean Dupont", response.getNomUtilisateur());

ArgumentCaptor<PaymentRequest> paymentCaptor = ArgumentCaptor.forClass(PaymentRequest.class);
verify(paymentServiceClient).initierPaiement(paymentCaptor.capture());
assertEquals(10L, paymentCaptor.getValue().getReferenceId());
assertEquals("VISITE", paymentCaptor.getValue().getReferenceType());
assertEquals(10000.0, paymentCaptor.getValue().getMontant());
}

@Test
void confirmer_passeEnPayeeSiEscrowAutorise() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.EN_ATTENTE).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

PaymentCallbackRequest callback = new PaymentCallbackRequest();
callback.setStatut("AUTORISE");
callback.setPaymentId("pay_1");

VisitResponse response = visitService.confirmer(1L, callback);

assertEquals(VisitStatut.PAYEE, response.getStatut());
assertEquals("pay_1", response.getPaymentId());
}

@Test
void confirmer_resteEnAttenteSiEscrowEchoue() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.EN_ATTENTE).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

PaymentCallbackRequest callback = new PaymentCallbackRequest();
callback.setStatut("ECHEC");

VisitResponse response = visitService.confirmer(1L, callback);

assertEquals(VisitStatut.EN_ATTENTE, response.getStatut());
}

@Test
void terminer_captureLesFondsEtPasseEnTermine() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.PAYEE).paymentId("5")
.clientEmail("client@test.com").agentEmail("agent@test.com").bienId(7L).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));
when(userServiceClient.getAuthUserIdByEmail("client@test.com")).thenReturn(42L);
when(userServiceClient.getAuthUserIdByEmail("agent@test.com")).thenReturn(99L);

VisitResponse response = visitService.terminer(1L);

assertEquals(VisitStatut.TERMINE, response.getStatut());
verify(paymentServiceClient).capturer(5L);

ArgumentCaptor<TransactionTermineeEvent> captor = ArgumentCaptor.forClass(TransactionTermineeEvent.class);
verify(transactionEventProducer).publier(captor.capture());
TransactionTermineeEvent event = captor.getValue();
assertEquals(1L, event.getTransactionId());
assertEquals(42L, event.getClientId());
assertEquals(99L, event.getAgentId());
assertEquals(7L, event.getBienId());
assertEquals("VISITE", event.getTypeTransaction());
}

@Test
void terminer_soucisKafka_neCassePasLaCloture() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.PAYEE).paymentId("5")
.clientEmail("client@test.com").agentEmail("agent@test.com").bienId(7L).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));
// La publication échoue : la visite doit tout de même être clôturée (best-effort).
org.mockito.Mockito.doThrow(new RuntimeException("broker down"))
.when(transactionEventProducer).publier(any());

VisitResponse response = visitService.terminer(1L);

assertEquals(VisitStatut.TERMINE, response.getStatut());
verify(paymentServiceClient).capturer(5L);
}

@Test
void nonPresente_captureLesFondsEtPasseEnNonPresentee() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.PAYEE).paymentId("5").build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

VisitResponse response = visitService.nonPresente(1L);

assertEquals(VisitStatut.NON_PRESENTEE, response.getStatut());
verify(paymentServiceClient).capturer(5L);
}

@Test
void annuler_rembourseSiPlusDe24hAvantLeDebut() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.PAYEE).paymentId("5")
.dateHeureDebut(LocalDateTime.now().plusDays(3)).annulationVerrouillee(false).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

VisitResponse response = visitService.annuler(1L);

assertEquals(VisitStatut.ANNULEE_CLIENT, response.getStatut());
verify(paymentServiceClient).rembourser(5L);
}

@Test
void annuler_refuseSiMoinsDe24hAvantLeDebut() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.PAYEE).paymentId("5")
.dateHeureDebut(LocalDateTime.now().plusHours(2)).annulationVerrouillee(false).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));

assertThrows(AnnulationImpossibleException.class, () -> visitService.annuler(1L));
verifyNoInteractions(paymentServiceClient);
}

@Test
void annuler_refuseSiVerrouillee() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.PAYEE).paymentId("5")
.dateHeureDebut(LocalDateTime.now().plusDays(3)).annulationVerrouillee(true).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));

assertThrows(AnnulationImpossibleException.class, () -> visitService.annuler(1L));
}

@Test
void annulerParAgent_rembourseSansConditionDeDelai() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.PAYEE).paymentId("5")
.dateHeureDebut(LocalDateTime.now().plusHours(1)).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

VisitResponse response = visitService.annulerParAgent(1L);

assertEquals(VisitStatut.ANNULEE_AGENT, response.getStatut());
verify(paymentServiceClient).rembourser(5L);
}

@Test
void annuler_visiteEnAttente_annuleSansAppelerLePaiement() {
// paymentId null : aucun escrow -> pas de remboursement, pas de NumberFormatException (Bug 1)
Visit visit = Visit.builder().id(1L).statut(VisitStatut.EN_ATTENTE)
.dateHeureDebut(LocalDateTime.now().plusHours(2)).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

VisitResponse response = visitService.annuler(1L);

assertEquals(VisitStatut.ANNULEE_CLIENT, response.getStatut());
verifyNoInteractions(paymentServiceClient);
}

@Test
void annulerParAgent_visiteEnAttente_annuleSansAppelerLePaiement() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.EN_ATTENTE)
.dateHeureDebut(LocalDateTime.now().plusHours(2)).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
when(visitRepository.save(any(Visit.class))).thenAnswer(inv -> inv.getArgument(0));

VisitResponse response = visitService.annulerParAgent(1L);

assertEquals(VisitStatut.ANNULEE_AGENT, response.getStatut());
verifyNoInteractions(paymentServiceClient);
}

@Test
void annuler_refuseSiVisiteDejaTerminee() {
Visit visit = Visit.builder().id(1L).statut(VisitStatut.TERMINE).paymentId("5")
.dateHeureDebut(LocalDateTime.now().plusDays(1)).build();
when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));

assertThrows(IllegalStateException.class, () -> visitService.annuler(1L));
verifyNoInteractions(paymentServiceClient);
}

@Test
void getById_leveUneExceptionSiVisiteIntrouvable() {
when(visitRepository.findById(99L)).thenReturn(Optional.empty());

assertThrows(RuntimeException.class, () -> visitService.getById(99L));
}

@Test
void getAvailableSlots_genereLesCreneauxLibresSurUneJournee() {
LocalDate lundi = LocalDate.of(2026, 7, 20);

when(bienServiceClient.getBienById(1L)).thenReturn(bien("VILLA"));

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

lenient().when(visitRepository.findByAgentEmailAndStatutNotIn(anyString(), anyList()))
.thenReturn(List.of());

List<SlotResponse> slots = visitService.getAvailableSlots(1L, lundi);

assertEquals(2, slots.size());
}
}
