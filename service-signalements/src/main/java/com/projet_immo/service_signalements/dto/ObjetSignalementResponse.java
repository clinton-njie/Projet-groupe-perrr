package com.projet_immo.service_signalements.dto;

import com.projet_immo.service_signalements.entity.ObjetSignalement;
import com.projet_immo.service_signalements.enums.SignalementType;

public class ObjetSignalementResponse {
    private Long id;
    private String libelle;
    private SignalementType type;
    private boolean actif;

    public ObjetSignalementResponse(Long id, String libelle, SignalementType type, boolean actif) {
        this.id = id;
        this.libelle = libelle;
        this.type = type;
        this.actif = actif;
    }

    public static ObjetSignalementResponse from(ObjetSignalement o) {
        return new ObjetSignalementResponse(o.getId(), o.getLibelle(), o.getType(), o.isActif());
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
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
    public boolean isActif() {
        return actif;
    }
    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
