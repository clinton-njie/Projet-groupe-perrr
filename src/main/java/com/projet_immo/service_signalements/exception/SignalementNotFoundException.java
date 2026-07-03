package com.projet_immo.service_signalements.exception;

public class SignalementNotFoundException extends RuntimeException {

    public SignalementNotFoundException(Long id) {
        super("Aucun signalement trouvé avec l'identifiant " + id + ".");
    }

}