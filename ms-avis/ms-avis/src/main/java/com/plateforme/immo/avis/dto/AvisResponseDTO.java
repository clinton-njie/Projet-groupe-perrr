package com.plateforme.immo.avis.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // <-- À RAJOUTER
import java.util.List;
import com.plateforme.immo.avis.model.Avis;

@Data
@NoArgsConstructor
@AllArgsConstructor // <-- À RAJOUTER
public class AvisResponseDTO {
    private List<Avis> avis;
    private double noteMoyenneGlobale;
    private double noteMoyenneCommunication;
    private double noteMoyenneProfessionnalisme;
    private double noteMoyenneDisponibilite;
    private double noteMoyenneConformiteAnnonce;
    private double noteMoyenneEtatGeneral;
    private double noteMoyenneRapportQualitePrix;
}