package com.projet_immo.service_signalements.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projet_immo.service_signalements.dto.GetSignalementResponse;
import com.projet_immo.service_signalements.dto.SignalementRequest;
import com.projet_immo.service_signalements.dto.SignalementResponse;
import com.projet_immo.service_signalements.entity.Signalement;
import com.projet_immo.service_signalements.enums.SignalementStatut;
import com.projet_immo.service_signalements.enums.SignalementType;
import com.projet_immo.service_signalements.feignclient.ReservationClient;
import com.projet_immo.service_signalements.feignclient.UtilisateurClient;
import com.projet_immo.service_signalements.feignclient.VisiteClient;
import com.projet_immo.service_signalements.repository.SignalementRepository;

@Service
public class SignalementService {
    @Autowired
    private SignalementRepository signalementRepository;
    @Autowired
    private VisiteClient visiteClient;
    @Autowired
    private ReservationClient reservationClient;
    @Autowired
    private UtilisateurClient utilisateurClient;

    public SignalementResponse addSignalement(SignalementRequest request, Long id, String role) {
        Long agentId = request.getAgentId();
        Long nbSignalement = signalementRepository.countByUtilisateurIdAndDateSignalement(id, LocalDate.now());
        Signalement signalement = new Signalement();
        if (nbSignalement >= 3) {
            throw new RuntimeException("Vous avez atteint votre limite de signalement journaliere");
        }
        if (request.getType().equals(SignalementType.AGENT)) {
            if (role.contains("AGENT")) {
                throw new RuntimeException("Un agent ne peut pas signaler un autre");
            } else {
                if (request.getAgentId() == null) {
                    throw new RuntimeException("Aucun agent selectionne");
                } else {
                    if (!visiteClient.existeInteraction(id, agentId)
                            && !reservationClient.existeInteraction(id, agentId)) {
                        throw new RuntimeException("Aucune interaction trouvée");
                    }
                }
                signalement.setAgentId(agentId);
            }
        }
        signalement.setTitre(request.getTitre());
        signalement.setType(request.getType());
        signalement.setDescription(request.getDescription());
        signalement.setUtilisateurId(id);
        signalement.setStatut(SignalementStatut.EN_ATTENTE);

        signalementRepository.save(signalement);

        String agentEmail = null;
        if (agentId != null) {
            agentEmail = utilisateurClient.getEmailById(agentId);
        }
        String utilisateurEmail = utilisateurClient.getEmailById(id);

        return new SignalementResponse(utilisateurEmail, agentEmail, "Signalement envoye");
    }

    public GetSignalementResponse getSignalement(Long signalementId) {
        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new RuntimeException("Signalement introuvable"));

        String utilisateurEmail = utilisateurClient.getEmailById(signalement.getUtilisateurId());

        String agentEmail = null;
        if (signalement.getAgentId() != null) {
            agentEmail = utilisateurClient.getEmailById(signalement.getAgentId());
        }

        return new GetSignalementResponse(
                signalement.getId(),
                signalement.getTitre(),
                signalement.getType(),
                signalement.getDescription(),
                signalement.getDateSignalement(),
                signalement.getStatut(),
                utilisateurEmail,
                agentEmail);
    }

    public List<GetSignalementResponse> getAllSignalements() {
        return signalementRepository.findAll()
                .stream()
                .map(signalement -> {
                    String utilisateurEmail = utilisateurClient.getEmailById(signalement.getUtilisateurId());

                    String agentEmail = null;
                    if (signalement.getAgentId() != null) {
                        agentEmail = utilisateurClient.getEmailById(signalement.getAgentId());
                    }

                    return new GetSignalementResponse(
                            signalement.getId(),
                            signalement.getTitre(),
                            signalement.getType(),
                            signalement.getDescription(),
                            signalement.getDateSignalement(),
                            signalement.getStatut(),
                            utilisateurEmail,
                            agentEmail);
                })
                .collect(Collectors.toList());
    }

    public GetSignalementResponse updateStatut(Long signalementId, SignalementStatut nouveauStatut) {
    Signalement signalement = signalementRepository.findById(signalementId)
        .orElseThrow(() -> new RuntimeException("Signalement introuvable"));

    if (signalement.getStatut().equals(SignalementStatut.RESOLU)
            || signalement.getStatut().equals(SignalementStatut.REJETE)) {
        throw new RuntimeException("Ce signalement est déjà traité");
    }

    signalement.setStatut(nouveauStatut);
    signalementRepository.save(signalement);

    String utilisateurEmail = utilisateurClient.getEmailById(signalement.getUtilisateurId());
    String agentEmail = null;
    if (signalement.getAgentId() != null) {
        agentEmail = utilisateurClient.getEmailById(signalement.getAgentId());
    }

    return new GetSignalementResponse(
        signalement.getId(),
        signalement.getTitre(),
        signalement.getType(),
        signalement.getDescription(),
        signalement.getDateSignalement(),
        signalement.getStatut(),
        utilisateurEmail,
        agentEmail
    );
}
}
