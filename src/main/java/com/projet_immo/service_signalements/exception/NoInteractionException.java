package com.projet_immo.service_signalements.exception;

public class NoInteractionException extends RuntimeException {

    public NoInteractionException() {
        super("Vous ne pouvez signaler que les agents avec lesquels vous avez déjà eu une interaction.");
    }
}