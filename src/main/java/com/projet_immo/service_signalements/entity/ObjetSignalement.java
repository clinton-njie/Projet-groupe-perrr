package com.projet_immo.service_signalements.entity;

import com.projet_immo.service_signalements.enums.SignalementType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "objetSignalements")
public class ObjetSignalement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String libelle;
    @Enumerated(EnumType.STRING)
    private SignalementType type;
    private boolean actif = true;

    public ObjetSignalement(){};

    public ObjetSignalement(String libelle, SignalementType type) {
        this.libelle = libelle;
        this.type = type;
        this.actif = true;
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
