package com.projet_immo.service_signalements.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.projet_immo.service_signalements.dto.GetSignalementResponse;
import com.projet_immo.service_signalements.dto.NotificationRequest;
import com.projet_immo.service_signalements.dto.SignalementRequest;
import com.projet_immo.service_signalements.dto.SignalementResponse;
import com.projet_immo.service_signalements.entity.ObjetSignalement;
import com.projet_immo.service_signalements.entity.Signalement;
import com.projet_immo.service_signalements.enums.SignalementStatut;
import com.projet_immo.service_signalements.enums.SignalementType;
import com.projet_immo.service_signalements.feignclient.NotificationClient;
import com.projet_immo.service_signalements.feignclient.ReservationClient;
import com.projet_immo.service_signalements.feignclient.UtilisateurClient;
import com.projet_immo.service_signalements.feignclient.VisiteClient;
import com.projet_immo.service_signalements.repository.ObjetSignalementRepository;
import com.projet_immo.service_signalements.repository.SignalementRepository;

@Service
public class SignalementService {

    private static final Logger log = LoggerFactory.getLogger(SignalementService.class);
    private static final int MAX_SIGNALEMENTS_PAR_JOUR = 3;

    @Autowired
    private SignalementRepository signalementRepository;
    @Autowired
    private ObjetSignalementRepository objetSignalementRepository;
    @Autowired
    private VisiteClient visiteClient;
    @Autowired
    private ReservationClient reservationClient;
    @Autowired
    private UtilisateurClient utilisateurClient;
    @Autowired
    private NotificationClient notificationClient;

    public SignalementResponse addSignalement(SignalementRequest request, Long utilisateurId, List<String> roles) {
        // Seuls ROLE_CLIENT et ROLE_AGENT peuvent signaler.
        if (roles == null || (!roles.contains("ROLE_CLIENT") && !roles.contains("ROLE_AGENT"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seuls les clients et les agents peuvent créer un signalement");
        }
        if (request.getType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le type de signalement est requis");
        }

        // Limite : 3 signalements par jour et par utilisateur.
        Long nbAujourdhui = signalementRepository.countByUtilisateurIdAndDateSignalement(utilisateurId, LocalDate.now());
        if (nbAujourdhui >= MAX_SIGNALEMENTS_PAR_JOUR) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Vous avez atteint votre limite de " + MAX_SIGNALEMENTS_PAR_JOUR + " signalements par jour");
        }

        // Objet : doit exister, être actif, et de type cohérent avec le signalement.
        if (request.getObjetSignalementId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'objet de signalement est requis");
        }
        ObjetSignalement objet = objetSignalementRepository.findById(request.getObjetSignalementId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Objet de signalement introuvable"));
        if (!objet.isActif()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet objet de signalement n'est plus disponible");
        }
        if (objet.getType() != request.getType()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'objet de signalement ne correspond pas au type de signalement");
        }

        Long agentId = null;
        if (request.getType() == SignalementType.AGENT) {
            agentId = request.getAgentId();
            if (agentId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun agent sélectionné");
            }
            if (agentId.equals(utilisateurId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vous ne pouvez pas vous signaler vous-même");
            }
            // Interaction préalable requise : une visite ou une réservation (CONFIRMEE/EXPIREE).
            if (!visiteClient.existeInteraction(utilisateurId, agentId)
                    && !reservationClient.existeInteraction(utilisateurId, agentId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Aucune interaction (visite ou réservation) trouvée avec cet agent");
            }
            // Pas de doublon EN_ATTENTE sur le même agent + même objet.
            if (signalementRepository.existsByUtilisateurIdAndAgentIdAndObjetSignalement_IdAndStatut(
                    utilisateurId, agentId, objet.getId(), SignalementStatut.EN_ATTENTE)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Un signalement en attente existe déjà contre cet agent pour ce motif");
            }
        }

        Signalement signalement = new Signalement();
        signalement.setType(request.getType());
        signalement.setObjetSignalement(objet);
        signalement.setDescription(request.getDescription());
        signalement.setUtilisateurId(utilisateurId);
        signalement.setAgentId(agentId);
        signalement.setStatut(SignalementStatut.EN_ATTENTE);
        signalementRepository.save(signalement);

        // Notifier les administrateurs (best-effort).
        notifier("ADMIN", "Nouveau signalement " + request.getType()
                + " en attente (motif: " + objet.getLibelle() + ")");

        String agentEmail = agentId != null ? getEmail(agentId) : null;
        String utilisateurEmail = getEmail(utilisateurId);
        return new SignalementResponse(utilisateurEmail, agentEmail, "Signalement envoyé");
    }

    public GetSignalementResponse updateStatut(Long signalementId, SignalementStatut nouveauStatut) {
        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Signalement introuvable"));

        // Un signalement ne peut changer de statut qu'à partir de EN_ATTENTE.
        if (signalement.getStatut() != SignalementStatut.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce signalement est déjà traité");
        }
        if (nouveauStatut != SignalementStatut.TERMINE_AVEC_SANCTION
                && nouveauStatut != SignalementStatut.TERMINE_SANS_SANCTION) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Statut cible invalide : attendu TERMINE_AVEC_SANCTION ou TERMINE_SANS_SANCTION");
        }

        signalement.setStatut(nouveauStatut);
        signalementRepository.save(signalement);

        // Sanction : bannissement de l'agent (désactivation + notification avec la raison).
        if (nouveauStatut == SignalementStatut.TERMINE_AVEC_SANCTION
                && signalement.getType() == SignalementType.AGENT
                && signalement.getAgentId() != null) {
            bannirAgent(signalement);
        }

        return toResponse(signalement);
    }

    public GetSignalementResponse getSignalement(Long signalementId) {
        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Signalement introuvable"));
        return toResponse(signalement);
    }

    public List<GetSignalementResponse> getAllSignalements() {
        return signalementRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // --- helpers ---

    // Le bannissement est déclenché après le changement de statut déjà committé : un souci
    // réseau ne doit pas annuler la décision de l'admin. On journalise sans relancer.
    private void bannirAgent(Signalement signalement) {
        Long agentId = signalement.getAgentId();
        String raison = signalement.getObjetSignalement().getLibelle();
        try {
            utilisateurClient.deactivate(agentId);
        } catch (Exception e) {
            log.warn("Echec désactivation de l'agent {} pour le signalement {}", agentId, signalement.getId(), e);
        }
        String agentEmail = getEmail(agentId);
        notifier(agentEmail != null ? agentEmail : String.valueOf(agentId),
                "Votre compte a été banni suite à un signalement fondé. Raison : " + raison);
    }

    private void notifier(String destinataire, String message) {
        try {
            notificationClient.notifier(new NotificationRequest(destinataire, message));
        } catch (Exception e) {
            log.warn("Echec envoi notification à {} : {}", destinataire, e.getMessage());
        }
    }

    private String getEmail(Long userId) {
        try {
            return utilisateurClient.getEmailById(userId);
        } catch (Exception e) {
            log.warn("Echec résolution email de l'utilisateur {} : {}", userId, e.getMessage());
            return null;
        }
    }

    private GetSignalementResponse toResponse(Signalement s) {
        String utilisateurEmail = getEmail(s.getUtilisateurId());
        String agentEmail = s.getAgentId() != null ? getEmail(s.getAgentId()) : null;
        ObjetSignalement objet = s.getObjetSignalement();
        return new GetSignalementResponse(
                s.getId(),
                objet != null ? objet.getId() : null,
                objet != null ? objet.getLibelle() : null,
                s.getType(),
                s.getDescription(),
                s.getDateSignalement(),
                s.getStatut(),
                utilisateurEmail,
                agentEmail);
    }
}
