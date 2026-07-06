package com.projet_immo.service_signalements.dto;

import com.projet_immo.service_signalements.enums.SignalementType;

// Création / modification d'un objet de signalement (réservé aux administrateurs).
public class ObjetSignalementRequest {
    private String libelle;
    private SignalementType type;

    public String getLibelle() {
        return libelle;
    }
    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
    public SignalementType getType() {
        return type;
    }
    public void setType(SignalementType type) {
        this.type = type;
    }
}
