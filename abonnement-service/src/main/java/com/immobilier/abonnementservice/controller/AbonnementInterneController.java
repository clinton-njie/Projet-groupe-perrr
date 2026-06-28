package com.immobilier.abonnementservice.controller;

import com.immobilier.abonnementservice.repository.AbonnementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interne/abonnements")
@RequiredArgsConstructor
public class AbonnementInterneController {

    private final AbonnementRepository repository;

    @GetMapping("/health")
    public String health() {
        return "ABONNEMENT-SERVICE OK";
    }

    @GetMapping("/count")
    public long count() {
        return repository.count();
    }
}