package com.microservices.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// Notification générique (destinataire + message libre), utilisée par ex. par service-signalements.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericNotificationRequest {

    @NotBlank(message = "Le destinataire est obligatoire")
    private String destinataire;

    @NotBlank(message = "Le message est obligatoire")
    private String message;
}
