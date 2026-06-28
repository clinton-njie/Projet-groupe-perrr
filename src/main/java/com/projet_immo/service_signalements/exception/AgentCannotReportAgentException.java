package com.projet_immo.service_signalements.exception;

public class AgentCannotReportAgentException extends RuntimeException {

    public AgentCannotReportAgentException() {
        super("Un agent ne peut pas signaler un autre agent.");
    }
}