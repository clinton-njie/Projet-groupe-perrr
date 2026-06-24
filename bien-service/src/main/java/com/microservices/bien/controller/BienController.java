package com.microservices.bien.controller;

import com.microservices.bien.dto.BienRequest;
import com.microservices.bien.dto.BienResponse;
import com.microservices.bien.dto.MessageResponse;
import com.microservices.bien.service.BienService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/biens")
@RequiredArgsConstructor
public class BienController {

    private final BienService bienService;

    // POST /api/biens → Créer un bien (AGENT/ADMIN)
    @PostMapping
    public ResponseEntity<BienResponse> creer(
            @Valid @RequestBody BienRequest request,
            Authentication auth
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bienService.creerBien(request, auth));
    }

    // GET /api/biens → Lister tous les biens (PUBLIC)
    @GetMapping
    public ResponseEntity<List<BienResponse>> lister() {
        return ResponseEntity.ok(bienService.listerBiens());
    }

    // GET /api/biens/disponibles → Biens disponibles (PUBLIC)
    @GetMapping("/disponibles")
    public ResponseEntity<List<BienResponse>> disponibles() {
        return ResponseEntity.ok(bienService.listerBiensDisponibles());
    }

    // GET /api/biens/{id} → Voir un bien (PUBLIC)
    @GetMapping("/{id}")
    public ResponseEntity<BienResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bienService.getBienById(id));
    }

    // PUT /api/biens/{id} → Modifier un bien (AGENT propriétaire / ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<BienResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody BienRequest request,
            Authentication auth
    ) {
        return ResponseEntity.ok(bienService.modifierBien(id, request, auth));
    }

    // DELETE /api/biens/{id} → Supprimer un bien (AGENT propriétaire / ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> supprimer(
            @PathVariable Long id,
            Authentication auth
    ) {
        return ResponseEntity.ok(bienService.supprimerBien(id, auth));
    }

    // GET /api/biens/categorie/{cat} → Filtrer par catégorie (PUBLIC)
    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<BienResponse>> parCategorie(@PathVariable String categorie) {
        return ResponseEntity.ok(bienService.getBiensByCategorie(categorie));
    }

    // GET /api/biens/recherche?localisation=... → Recherche (PUBLIC)
    @GetMapping("/recherche")
    public ResponseEntity<List<BienResponse>> rechercher(
            @RequestParam String localisation
    ) {
        return ResponseEntity.ok(bienService.rechercherParLocalisation(localisation));
    }

    // GET /api/biens/mes-biens → Biens de l'agent connecté (AGENT)
    @GetMapping("/mes-biens")
    public ResponseEntity<List<BienResponse>> mesBiens(Authentication auth) {
        return ResponseEntity.ok(bienService.mesBiens(auth));
    }
}
