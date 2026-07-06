package com.projet_immo.service_signalements.dto;

// Payload de l'endpoint générique POST /api/notifications de service-notification.
public class NotificationRequest {
    private String destinataire;
    private String message;

    public NotificationRequest() {
    }

    public NotificationRequest(String destinataire, String message) {
        this.destinataire = destinataire;
        this.message = message;
    }

    public String getDestinataire() {
        return destinataire;
    }
    public void setDestinataire(String destinataire) {
        this.destinataire = destinataire;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
