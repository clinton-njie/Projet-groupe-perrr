package com.immobilier.abonnementservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HistoriqueAbonnementResponse {

    private Long agentId;
    private List<AbonnementResponse> abonnements;

}