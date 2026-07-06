package com.projet_immo.service_signalements.dto;

import com.projet_immo.service_signalements.enums.SignalementType;

public class SignalementRequest {
    private SignalementType type;
    private Long objetSignalementId;
    private String description;
    // Requis uniquement pour un signalement de type AGENT (id de l'agent visé).
    private Long agentId;

    public SignalementType getType() {
        return type;
    }
    public void setType(SignalementType type) {
        this.type = type;
    }
    public Long getObjetSignalementId() {
        return objetSignalementId;
    }
    public void setObjetSignalementId(Long objetSignalementId) {
        this.objetSignalementId = objetSignalementId;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Long getAgentId() {
        return agentId;
    }
    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }
}
