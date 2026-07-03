package com.projet_immo.service_signalements.exception;

public class AgentNotSelectedException extends RuntimeException {

    public AgentNotSelectedException() {
        super("Veuillez sélectionner un agent.");
    }

}