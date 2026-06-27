package com.immobilier.reservationservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentDecisionRequest {

    private Long agentId; // ID de l'agent qui prend la décision

    private String comment; // Commentaire de l'agent sur la décision
}