package com.microservices.servicevisite.service;

import com.microservices.servicevisite.dto.*;
import com.microservices.servicevisite.entity.*;
import com.microservices.servicevisite.enums.*;
import com.microservices.servicevisite.exception.AnnulationImpossibleException;
import com.microservices.servicevisite.feign.*;
import com.microservices.servicevisite.kafka.TransactionEventProducer;
import com.microservices.servicevisite.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VisitService {

private static final Logger log = LoggerFactory.getLogger(VisitService.class);

private final VisitRepository visitRepository;
private final AgentAvailabilityRepository availabilityRepository;
private final BienServiceClient bienServiceClient;
private final PaymentServiceClient paymentServiceClient;
private final UserServiceClient userServiceClient;
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

// Statuts qui libèrent le créneau : la visite n'occupe plus la case horaire de l'agent.
private static final List<VisitStatut> STATUTS_LIBERANT_CRENEAU =
List.of(VisitStatut.ANNULEE_CLIENT, VisitStatut.ANNULEE_AGENT, VisitStatut.NON_PRESENTEE);

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
STATUTS_LIBERANT_CRENEAU
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
// et payment-service rappelle /api/visites/{id}/confirmer dans la foulée. Si cette méthode était
// transactionnelle, la visite ne serait pas encore committée quand ce rappel arrive
// (findById échouerait avec "Visite non trouvée").
public VisitResponse creerVisite(VisitRequest request) {
BienResponse bien = bienServiceClient.getBienById(request.getBienId());
PrixVisiteResponse prixVisite = bienServiceClient.getPrixVisite(bien.getCategorie());

Long authUserId = userServiceClient.getAuthUserIdByEmail(request.getClientEmail());
UserServiceResponse profil = userServiceClient.getProfile(authUserId);
String nomUtilisateur = (profil.getFirstname() + " " + profil.getLastname()).trim();

Visit visit = Visit.builder()
.bienId(request.getBienId())
.clientEmail(request.getClientEmail())
.agentEmail(bien.getAgentEmail())
.dateHeureDebut(request.getDateHeureDebut())
.dateHeureFin(request.getDateHeureFin())
.statut(VisitStatut.EN_ATTENTE)
.createdAt(LocalDateTime.now())
.nomUtilisateur(nomUtilisateur)
.nomBien(bien.getNom())
.categorieBien(bien.getCategorie())
.operateur(request.getOperateur())
.telephone(request.getTelephone())
.build();

visit = visitRepository.save(visit);

PaymentRequest paymentRequest = new PaymentRequest(
visit.getId(), "VISITE", nomUtilisateur, bien.getNom(),
prixVisite.getPrixVisite().doubleValue(), request.getOperateur(), request.getTelephone()
);
paymentServiceClient.initierPaiement(paymentRequest);

return mapToResponse(visit);
}

// Callback paiement (payment-service confirme la mise en escrow des fonds)
@Transactional
public VisitResponse confirmer(Long id, PaymentCallbackRequest request) {
Visit visit = getVisitOrThrow(id);

if ("AUTORISE".equals(request.getStatut())) {
visit.setStatut(VisitStatut.PAYEE);
visit.setPaymentId(request.getPaymentId());
}
// Sinon la visite reste EN_ATTENTE ; le VisitTTLScheduler la nettoiera après 15 min.

return mapToResponse(visitRepository.save(visit));
}

// L'agent termine la visite -> transfert des fonds retenus à l'agent
@Transactional
public VisitResponse terminer(Long id) {
Visit visit = getVisitOrThrow(id);
exigerStatutPayeeOuEnCours(visit);

paymentServiceClient.capturer(Long.parseLong(visit.getPaymentId()));
visit.setStatut(VisitStatut.TERMINE);

Visit saved = visitRepository.save(visit);
publierTransactionTerminee(saved);
return mapToResponse(saved);
}

// Visite terminée avec succès -> ms-avis crée l'opportunité de notation.
// Best-effort : la notation est secondaire, un souci Kafka/résolution d'ID ne doit pas
// casser la clôture de la visite (fonds déjà capturés, statut déjà committé).
private void publierTransactionTerminee(Visit visit) {
try {
Long clientId = userServiceClient.getAuthUserIdByEmail(visit.getClientEmail());
Long agentId = userServiceClient.getAuthUserIdByEmail(visit.getAgentEmail());
transactionEventProducer.publier(TransactionTermineeEvent.builder()
.transactionId(visit.getId())
.clientId(clientId)
.agentId(agentId)
.bienId(visit.getBienId())
.typeTransaction("VISITE")
.build());
} catch (Exception e) {
log.warn("Echec publication transaction.terminee pour la visite {}", visit.getId(), e);
}
}

// L'agent constate l'absence du client -> transfert des fonds retenus à l'agent
@Transactional
public VisitResponse nonPresente(Long id) {
Visit visit = getVisitOrThrow(id);
exigerStatutPayeeOuEnCours(visit);

paymentServiceClient.capturer(Long.parseLong(visit.getPaymentId()));
visit.setStatut(VisitStatut.NON_PRESENTEE);

return mapToResponse(visitRepository.save(visit));
}

// Annulation par le client : gratuite si >=24h avant le début, sinon refusée
@Transactional
public VisitResponse annuler(Long id) {
Visit visit = getVisitOrThrow(id);

// Visite pas encore payée (aucun escrow) : annulation simple, rien à rembourser.
if (visit.getStatut() == VisitStatut.EN_ATTENTE) {
visit.setStatut(VisitStatut.ANNULEE_CLIENT);
return mapToResponse(visitRepository.save(visit));
}

// Au-delà, seule une visite payée / en cours peut être annulée (garde le paymentId non nul).
exigerStatutPayeeOuEnCours(visit);

if (visit.isAnnulationVerrouillee() || moinsDe24hAvantDebut(visit)) {
throw new AnnulationImpossibleException(
"Annulation impossible à moins de 24h du début de la visite");
}

paymentServiceClient.rembourser(Long.parseLong(visit.getPaymentId()));
visit.setStatut(VisitStatut.ANNULEE_CLIENT);

return mapToResponse(visitRepository.save(visit));
}

// Annulation par l'agent : remboursement intégral, sans restriction de délai
@Transactional
public VisitResponse annulerParAgent(Long id) {
Visit visit = getVisitOrThrow(id);

// Visite pas encore payée (aucun escrow) : annulation simple, rien à rembourser.
if (visit.getStatut() == VisitStatut.EN_ATTENTE) {
visit.setStatut(VisitStatut.ANNULEE_AGENT);
return mapToResponse(visitRepository.save(visit));
}

exigerStatutPayeeOuEnCours(visit);

paymentServiceClient.rembourser(Long.parseLong(visit.getPaymentId()));
visit.setStatut(VisitStatut.ANNULEE_AGENT);

return mapToResponse(visitRepository.save(visit));
}

public VisitResponse getById(Long id) {
return mapToResponse(getVisitOrThrow(id));
}

public List<VisitResponse> getByUser(String clientEmail) {
return visitRepository.findByClientEmail(clientEmail).stream()
.map(this::mapToResponse)
.toList();
}

public List<VisitResponse> getByAgent(String agentEmail) {
return visitRepository.findByAgentEmail(agentEmail).stream()
.map(this::mapToResponse)
.toList();
}

// Statuts d'une visite considérée comme une interaction effective client<->agent
// (le paiement a eu lieu / la visite s'est déroulée) : ouvre droit à un signalement d'agent.
private static final List<VisitStatut> STATUTS_INTERACTION =
List.of(VisitStatut.PAYEE, VisitStatut.EN_COURS, VisitStatut.TERMINE, VisitStatut.NON_PRESENTEE);

// Appelé par service-signalements. Les ids numériques sont résolus en emails (Visit stocke des emails).
public boolean existeInteraction(Long utilisateurId, Long agentId) {
String clientEmail = userServiceClient.getEmailById(utilisateurId);
String agentEmail = userServiceClient.getEmailById(agentId);
if (clientEmail == null || agentEmail == null) {
return false;
}
return visitRepository.existsByClientEmailAndAgentEmailAndStatutIn(clientEmail, agentEmail, STATUTS_INTERACTION);
}

private Visit getVisitOrThrow(Long id) {
return visitRepository.findById(id)
.orElseThrow(() -> new RuntimeException("Visite non trouvée"));
}

private void exigerStatutPayeeOuEnCours(Visit visit) {
if (visit.getStatut() != VisitStatut.PAYEE && visit.getStatut() != VisitStatut.EN_COURS) {
throw new IllegalStateException(
"La visite " + visit.getId() + " n'est pas dans un état permettant cette action (statut actuel : " + visit.getStatut() + ")");
}
}

private boolean moinsDe24hAvantDebut(Visit visit) {
return Duration.between(LocalDateTime.now(), visit.getDateHeureDebut()).toHours() < 24;
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
response.setNomUtilisateur(visit.getNomUtilisateur());
response.setNomBien(visit.getNomBien());
response.setAnnulationVerrouillee(visit.isAnnulationVerrouillee());
return response;
}
}
