package com.immobilier.abonnementservice.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaiementResponse {

    private Long id;
    private String reference;
    private String statut;
    private String message;

}