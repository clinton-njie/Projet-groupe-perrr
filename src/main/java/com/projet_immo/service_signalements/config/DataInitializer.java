package com.projet_immo.service_signalements.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.projet_immo.service_signalements.entity.ObjetSignalement;
import com.projet_immo.service_signalements.enums.SignalementType;
import com.projet_immo.service_signalements.repository.ObjetSignalementRepository;

@Component
public class DataInitializer implements CommandLineRunner{
    private final ObjetSignalementRepository signalementRepository;

    public DataInitializer(ObjetSignalementRepository signalementRepository){
        this.signalementRepository = signalementRepository;
    };

    private void ajouterSiAbsent(String libelle, SignalementType type){
        if (!signalementRepository.exiexistsByLibelleAndType(libelle, type)) {
            signalementRepository.save(new ObjetSignalement(libelle, type));
        }
    }

    @Override
    public void run(String... args){
        ajouterSiAbsent("Dysfonctionnement de l'application", SignalementType.TECHNIQUE);
        ajouterSiAbsent("Probleme d'affichage", SignalementType.TECHNIQUE);
        ajouterSiAbsent("Bug lors d'une operation", SignalementType.TECHNIQUE);
        ajouterSiAbsent("Probleme d'acces au compte", SignalementType.TECHNIQUE);
        ajouterSiAbsent("Temps de reponse trop long", SignalementType.TECHNIQUE);
        ajouterSiAbsent("Agent indisponible", SignalementType.AGENT);
        ajouterSiAbsent("Manque de professionalisme", SignalementType.AGENT);
        ajouterSiAbsent("Informations incorrectes fournis par l'agent", SignalementType.AGENT);
        ajouterSiAbsent("Demande abusive de frais", SignalementType.AGENT);
        ajouterSiAbsent("Traitement trop lent d'une demande", SignalementType.AGENT);
    }
}
