package com.projet_immo.service_signalements.dto;

public class SignalementResponse {
    private String utilisateurEmail;
    private String agentEmail;
    private String message;

    public String getUtilisateurEmail() {
        return utilisateurEmail;
    }

    public String getAgentEmail() {
        return agentEmail;
    }

    public String getMessage() {
        return message;
    }

    public SignalementResponse(String utilisateurEmail, String agentEmail, String message){
        this.utilisateurEmail = utilisateurEmail;
        this.agentEmail = agentEmail;
        this.message = message;
    }
}
