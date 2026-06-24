package com.microservices.bien.service;

import com.microservices.bien.dto.BienRequest;
import com.microservices.bien.dto.BienResponse;
import com.microservices.bien.dto.MessageResponse;
import com.microservices.bien.entity.Bien;
import com.microservices.bien.entity.Categorie;
import com.microservices.bien.repository.BienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BienService {

    private final BienRepository bienRepository;

    // ✅ Créer un bien
    @Transactional
    public BienResponse creerBien(BienRequest request, Authentication auth) {

        Bien bien = Bien.builder()
                .categorie(request.getCategorie())
                .nom(request.getNom())
                .localisation(request.getLocalisation())
                .image(request.getImage())
                .prix(request.getPrix())
                .description(request.getDescription())
                .agentEmail(auth.getName())
                .agentId(extractUserIdFromEmail(auth.getName()))
                .disponible(true)
                .build();

        Bien saved = bienRepository.save(bien);
        return toResponse(saved);
    }

    // ✅ Lister tous les biens
    public List<BienResponse> listerBiens() {
        return bienRepository.findAll().stream().map(this::toResponse).toList();
    }

    // ✅ Lister les biens disponibles
    public List<BienResponse> listerBiensDisponibles() {
        return bienRepository.findByDisponible(true).stream().map(this::toResponse).toList();
    }

    // ✅ Voir un bien par ID
    public BienResponse getBienById(Long id) {
        Bien bien = bienRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Bien introuvable avec l'id : " + id));
        return toResponse(bien);
    }

    // ✅ Modifier un bien
    @Transactional
    public BienResponse modifierBien(Long id, BienRequest request, Authentication auth) {

        Bien bien = bienRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Bien introuvable avec l'id : " + id));

        // Seul l'agent propriétaire ou un ADMIN peut modifier
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isOwner = bien.getAgentEmail().equals(auth.getName());

        if (!isAdmin && !isOwner) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à modifier ce bien");
        }

        bien.setCategorie(request.getCategorie());
        bien.setNom(request.getNom());
        bien.setLocalisation(request.getLocalisation());
        bien.setImage(request.getImage());
        bien.setPrix(request.getPrix());
        bien.setDescription(request.getDescription());

        return toResponse(bienRepository.save(bien));
    }

    // ✅ Supprimer un bien
    @Transactional
    public MessageResponse supprimerBien(Long id, Authentication auth) {

        Bien bien = bienRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Bien introuvable avec l'id : " + id));

        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isOwner = bien.getAgentEmail().equals(auth.getName());

        if (!isAdmin && !isOwner) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à supprimer ce bien");
        }

        bienRepository.delete(bien);

        return MessageResponse.builder()
                .message("Bien supprimé avec succès")
                .build();
    }

    // ✅ Filtrer par catégorie
    public List<BienResponse> getBiensByCategorie(String categorie) {
        Categorie cat = Categorie.valueOf(categorie.toUpperCase());
        return bienRepository.findByCategorie(cat).stream().map(this::toResponse).toList();
    }

    // ✅ Rechercher par localisation
    public List<BienResponse> rechercherParLocalisation(String localisation) {
        return bienRepository.findByLocalisationContainingIgnoreCase(localisation)
                .stream().map(this::toResponse).toList();
    }

    // ✅ Mes biens (agent connecté)
    public List<BienResponse> mesBiens(Authentication auth) {
        Long agentId = extractUserIdFromEmail(auth.getName());
        return bienRepository.findByAgentId(agentId).stream().map(this::toResponse).toList();
    }

    // Mapper entité → DTO
    private BienResponse toResponse(Bien bien) {
        return BienResponse.builder()
                .id(bien.getId())
                .categorie(bien.getCategorie())
                .nom(bien.getNom())
                .localisation(bien.getLocalisation())
                .image(bien.getImage())
                .prix(bien.getPrix())
                .date(bien.getDate())
                .description(bien.getDescription())
                .agentId(bien.getAgentId())
                .agentEmail(bien.getAgentEmail())
                .disponible(bien.getDisponible())
                .build();
    }

    // Simuler extraction de l'ID depuis l'email (hashCode pour simplifier)
    private Long extractUserIdFromEmail(String email) {
        return (long) Math.abs(email.hashCode());
    }
}
