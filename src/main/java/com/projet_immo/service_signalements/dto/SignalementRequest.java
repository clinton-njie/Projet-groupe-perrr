package com.projet_immo.service_signalements.dto;

import java.time.LocalDate;

import com.projet_immo.service_signalements.enums.SignalementStatut;
import com.projet_immo.service_signalements.enums.SignalementType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SignalementRequest {
    @NotBlank(message = "Le titre est obligatoire.")
    @Size(max = 100, message = "Le titre ne doit pas dépasser 100 caractères.")
    private String titre;
    @NotNull(message = "Le type de signalement est obligatoire.")
    private SignalementType type;
    @NotBlank(message = "La description est obligatoire.")
    private String description;
    private LocalDate dateSignalement;
    private SignalementStatut statut;
    private Long utilisateurId;
    private Long agentId;
    
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
    public Long getUtilisateurId() {
        return utilisateurId;
    }
    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }
    public Long getAgentId() {
        return agentId;
    }
    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }
}
