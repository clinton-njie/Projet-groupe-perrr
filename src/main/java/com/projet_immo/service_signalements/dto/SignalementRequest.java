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
    private Long objetSignalement;
    @NotNull(message = "Le type de signalement est obligatoire.")
    private SignalementType type;
    @NotBlank(message = "La description est obligatoire.")
    private String description;
    private Long utilisateurId;
    private Long agentId;
    
    public Long getObjetSignalement() {
        return objetSignalement;
    }
    public void setObjetSignalement(Long objetSignalement) {
        this.objetSignalement = objetSignalement;
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
