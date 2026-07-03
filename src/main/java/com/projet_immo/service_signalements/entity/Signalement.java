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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "signalements")
public class Signalement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    
    @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name = "objet_signalement_id")
    private ObjetSignalement objetSignalement;

    public Signalement() {
    };


    public Signalement(Long id, SignalementType type, String description, LocalDate dateSignalement,
            SignalementStatut statut, Long utilisateurId, Long agentId, ObjetSignalement objetSignalement) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.dateSignalement = dateSignalement;
        this.statut = statut;
        this.utilisateurId = utilisateurId;
        this.agentId = agentId;
        this.objetSignalement = objetSignalement;
    }

    public ObjetSignalement getObjetSignalement() {
        return objetSignalement;
    }


    public void setObjetSignalement(ObjetSignalement objetSignalement) {
        this.objetSignalement = objetSignalement;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
