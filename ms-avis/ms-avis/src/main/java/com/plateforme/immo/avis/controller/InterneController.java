package com.plateforme.immo.avis.controller;

import com.plateforme.immo.avis.dto.TransactionInterneDTO;
import com.plateforme.immo.avis.model.TypeTransaction;
import com.plateforme.immo.avis.service.OpportuniteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interne")
public class InterneController {

    @Autowired
    private OpportuniteService opportuniteService;

    @PostMapping("/transactions")
    public ResponseEntity<String> declencherOpportunite(@Valid @RequestBody TransactionInterneDTO dto) {
        TypeTransaction type = TypeTransaction.valueOf(dto.getTypeTransaction());
        opportuniteService.creerOpportunite(dto.getClientId(), dto.getAgentId(), dto.getBienId(), dto.getTransactionId(), type);
        return ResponseEntity.status(HttpStatus.CREATED).body("Opportunité de notation créée avec succès");
    }
}