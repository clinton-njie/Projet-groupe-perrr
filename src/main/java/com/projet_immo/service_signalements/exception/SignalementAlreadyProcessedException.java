package com.projet_immo.service_signalements.exception;

public class SignalementAlreadyProcessedException extends RuntimeException {

    public SignalementAlreadyProcessedException() {
        super("Ce signalement a déjà été traité ou rejeté.");
    }

}