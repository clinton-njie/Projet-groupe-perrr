package com.projet_immo.service_signalements.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.projet_immo.service_signalements.entity.ObjetSignalement;
import com.projet_immo.service_signalements.enums.SignalementType;
import com.projet_immo.service_signalements.repository.ObjetSignalementRepository;

/**
 * Initialise la table objet_signalement au démarrage avec des motifs prédéfinis
 * (AGENT + TECHNIQUE) si elle est vide, pour garantir des motifs immédiatement
 * disponibles. Les administrateurs peuvent ensuite en ajouter / désactiver.
 */
@Component
public class ObjetSignalementSeeder implements CommandLineRunner {

    private final ObjetSignalementRepository repository;

    public ObjetSignalementSeeder(ObjetSignalementRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.saveAll(List.of(
                new ObjetSignalement("Comportement inapproprié de l'agent", SignalementType.AGENT, true),
                new ObjetSignalement("Agent injoignable", SignalementType.AGENT, true),
                new ObjetSignalement("Annonce non conforme au bien visité", SignalementType.AGENT, true),
                new ObjetSignalement("Demande de paiement hors plateforme", SignalementType.AGENT, true),
                new ObjetSignalement("Fraude ou arnaque suspectée", SignalementType.AGENT, true),
                new ObjetSignalement("Bug d'affichage", SignalementType.TECHNIQUE, true),
                new ObjetSignalement("Erreur lors du paiement", SignalementType.TECHNIQUE, true),
                new ObjetSignalement("Problème de connexion / authentification", SignalementType.TECHNIQUE, true),
                new ObjetSignalement("Fonctionnalité indisponible", SignalementType.TECHNIQUE, true),
                new ObjetSignalement("Autre problème technique", SignalementType.TECHNIQUE, true)));
    }
}
