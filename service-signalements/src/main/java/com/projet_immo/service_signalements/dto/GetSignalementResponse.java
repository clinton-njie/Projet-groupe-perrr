package com.projet_immo.service_signalements.dto;

import java.time.LocalDate;

import com.projet_immo.service_signalements.enums.SignalementStatut;
import com.projet_immo.service_signalements.enums.SignalementType;

public class GetSignalementResponse {
    private Long id;
    private String titre;
    private SignalementType type;
    private String description;
    private LocalDate dateSignalement;
    private SignalementStatut statut;
    private String utilisateurEmail;
    private String agentEmail;

    public String getUtilisateurEmail() {
        return utilisateurEmail;
    }

    public void setUtilisateurEmail(String utilisateurEmail) {
        this.utilisateurEmail = utilisateurEmail;
    }

    public String getAgentEmail() {
        return agentEmail;
    }

    public void setAgentEmail(String agentEmail) {
        this.agentEmail = agentEmail;
    }

    public GetSignalementResponse(Long id, String titre, SignalementType type, String description,
            LocalDate dateSignalement, SignalementStatut statut, String utilisateurEmail, String agentEmail) {
        this.id = id;
        this.titre = titre;
        this.type = type;
        this.description = description;
        this.dateSignalement = dateSignalement;
        this.statut = statut;
        this.utilisateurEmail = utilisateurEmail;
        this.agentEmail = agentEmail;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitre() {
        return titre;
    }
    public void setTitre(String titre) {
        this.titre = titre;
    }
    public SignalementType getType() {
        return type;
    }
    public void setType(SignalementType type) {
        this.type = type;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public LocalDate getDateSignalement() {
        return dateSignalement;
    }
    public void setDateSignalement(LocalDate dateSignalement) {
        this.dateSignalement = dateSignalement;
    }
    public SignalementStatut getStatut() {
        return statut;
    }
    public void setStatut(SignalementStatut statut) {
        this.statut = statut;
    }
}
