package com.projet_immo.service_signalements.exception;

public class DailyLimitExceededException extends RuntimeException {

    public DailyLimitExceededException() {
        super("Vous avez atteint la limite de trois signalements pour aujourd'hui.");
    }
}
