package com.projet_immo.service_signalements.entity;

import com.projet_immo.service_signalements.enums.SignalementType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Motif de signalement proposé aux utilisateurs. Jamais supprimé : quand il ne doit plus
 * être proposé, on passe simplement {@code actif} à false. Un objet de type AGENT ne peut
 * cadrer qu'un signalement AGENT, idem pour TECHNIQUE.
 */
@Entity
@Table(name = "objet_signalement")
public class ObjetSignalement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String libelle;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SignalementType type;

    @Column(nullable = false)
    private boolean actif = true;

    public ObjetSignalement() {
    }

    public ObjetSignalement(String libelle, SignalementType type, boolean actif) {
        this.libelle = libelle;
        this.type = type;
        this.actif = actif;
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
