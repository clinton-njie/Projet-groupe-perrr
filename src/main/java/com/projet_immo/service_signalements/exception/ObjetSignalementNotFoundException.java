package com.projet_immo.service_signalements.exception;

public class ObjetSignalementNotFoundException extends RuntimeException {

    public ObjetSignalementNotFoundException(Long id) {
        super("Aucun objet de signalement trouvé avec l'identifiant " + id + ".");
    }

}