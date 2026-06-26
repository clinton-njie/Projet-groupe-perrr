package com.projet_immo.service_signalements.entity;

import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import com.projet_immo.service_signalements.enums.SignalementStatut;
import com.projet_immo.service_signalements.enums.SignalementType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "signalements")
public class Signalement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SignalementType type;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDate dateSignalement;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SignalementStatut statut;
    
    @Column(nullable = false)
    private Long utilisateurId;
    private Long agentId;

    public Signalement() {
    };

    public Signalement(Long id, String titre, SignalementType type, String description, LocalDate dateSignalement,
            SignalementStatut statut, Long utilisateurId, Long agentId) {
        this.id = id;
        this.titre = titre;
        this.type = type;
        this.description = description;
        this.dateSignalement = dateSignalement;
        this.statut = statut;
        this.utilisateurId = utilisateurId;
        this.agentId = agentId;
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
