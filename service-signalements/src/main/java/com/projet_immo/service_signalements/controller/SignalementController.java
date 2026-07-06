package com.projet_immo.service_signalements.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projet_immo.service_signalements.dto.GetSignalementResponse;
import com.projet_immo.service_signalements.dto.ObjetSignalementRequest;
import com.projet_immo.service_signalements.dto.ObjetSignalementResponse;
import com.projet_immo.service_signalements.dto.SignalementRequest;
import com.projet_immo.service_signalements.dto.SignalementResponse;
import com.projet_immo.service_signalements.dto.StatutRequest;
import com.projet_immo.service_signalements.enums.SignalementType;
import com.projet_immo.service_signalements.feignclient.UtilisateurClient;
import com.projet_immo.service_signalements.service.ObjetSignalementService;
import com.projet_immo.service_signalements.service.SignalementService;

@RestController
@RequestMapping("/signalements")
public class SignalementController {

    @Autowired
    private SignalementService signalementService;
    @Autowired
    private ObjetSignalementService objetSignalementService;
    @Autowired
    private UtilisateurClient utilisateurClient;

    @PostMapping
    public ResponseEntity<SignalementResponse> addSignalement(@RequestBody SignalementRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Le JWT ne porte que l'email (principal) ; on résout l'id numérique via user-service.
        String email = auth.getName();
        Long userId = utilisateurClient.getAuthUserIdByEmail(email);
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        SignalementResponse response = signalementService.addSignalement(request, userId, roles);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetSignalementResponse> getSignalement(@PathVariable Long id) {
        return ResponseEntity.ok(signalementService.getSignalement(id));
    }

    @GetMapping
    public ResponseEntity<List<GetSignalementResponse>> getAllSignalements() {
        return ResponseEntity.ok(signalementService.getAllSignalements());
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<GetSignalementResponse> updateStatut(
            @PathVariable Long id,
            @RequestBody StatutRequest statutRequest) {
        return ResponseEntity.ok(signalementService.updateStatut(id, statutRequest.getStatut()));
    }

    // --- Objets de signalement ---

    // Liste des motifs actifs (consultable par tout utilisateur authentifié), filtrable par type.
    @GetMapping("/objets")
    public ResponseEntity<List<ObjetSignalementResponse>> listerObjets(
            @RequestParam(required = false) SignalementType type) {
        return ResponseEntity.ok(objetSignalementService.listerActifs(type));
    }

    // Création d'un motif (admin uniquement, cf. SecurityConfig).
    @PostMapping("/objets")
    public ResponseEntity<ObjetSignalementResponse> creerObjet(@RequestBody ObjetSignalementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(objetSignalementService.creer(request));
    }

    // Modification d'un motif (admin uniquement).
    @PutMapping("/objets/{id}")
    public ResponseEntity<ObjetSignalementResponse> modifierObjet(
            @PathVariable Long id,
            @RequestBody ObjetSignalementRequest request) {
        return ResponseEntity.ok(objetSignalementService.modifier(id, request));
    }

    // Désactivation d'un motif (jamais de suppression) (admin uniquement).
    @PutMapping("/objets/{id}/desactiver")
    public ResponseEntity<ObjetSignalementResponse> desactiverObjet(@PathVariable Long id) {
        return ResponseEntity.ok(objetSignalementService.desactiver(id));
    }
}
