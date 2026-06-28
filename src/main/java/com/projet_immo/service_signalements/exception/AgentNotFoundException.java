package com.projet_immo.service_signalements.exception;

public class AgentNotFoundException extends RuntimeException {

    public AgentNotFoundException(Long id) {
        super("Aucun agent trouvé avec l'identifiant " + id + ".");
    }
}