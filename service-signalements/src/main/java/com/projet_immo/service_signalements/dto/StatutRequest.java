package com.projet_immo.service_signalements.dto;

import com.projet_immo.service_signalements.enums.SignalementStatut;

public class StatutRequest {
    private SignalementStatut statut;

    public SignalementStatut getStatut() { return statut; }
    public void setStatut(SignalementStatut statut) { this.statut = statut; }
}