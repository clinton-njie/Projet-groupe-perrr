package com.immobilier.userservice.dto.response;

import com.immobilier.userservice.enums.StatutDemande;
import lombok.Data;

import java.time.LocalDateTime;

// DTO pour la réponse de la demande d'agent. 
// Contient les détails de la demande, y compris le motif, le statut et la date de création.
@Data
public class DemandeAgentResponse {

    private Long id;
    private String motif;
    private StatutDemande statut;
    private LocalDateTime dateCreation;
}