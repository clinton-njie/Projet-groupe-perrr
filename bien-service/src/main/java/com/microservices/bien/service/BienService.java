package com.microservices.bien.service;

import com.microservices.bien.dto.*;
import com.microservices.bien.entity.Bien;
import com.microservices.bien.entity.Categorie;
import com.microservices.bien.entity.ImageBien;
import com.microservices.bien.entity.PrixVisite;
import com.microservices.bien.entity.StatutBien;
import com.microservices.bien.repository.BienRepository;
import com.microservices.bien.repository.PrixVisiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BienService {

    private final BienRepository bienRepository;
    private final PrixVisiteRepository prixVisiteRepository;
    private final ImageStorageService imageStorageService;

    // ✅ Créer un bien
    @Transactional
    public BienResponse creerBien(BienRequest request, Authentication auth) {

        validerNombrePieces(request.getCategorie(), request.getNombrePiecesDisponibles());

        Bien bien = Bien.builder()
                .categorie(request.getCategorie())
                .nom(request.getNom())
                .ville(request.getVille())
                .quartier(request.getQuartier())
                .photoPrincipale(request.getPhotoPrincipale())
                .imagePublicId(request.getImagePublicId())
                .photosSecondaires(toEntityImages(request.getPhotosSecondaires()))
                .prixMensuel(request.getPrixMensuel())
                .nombrePiecesDisponibles(request.getNombrePiecesDisponibles())
                .description(request.getDescription())
                .agentEmail(auth.getName())
                .statut(StatutBien.DISPONIBLE)
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
        return bienRepository.findByStatut(StatutBien.DISPONIBLE).stream().map(this::toResponse).toList();
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

        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isOwner = bien.getAgentEmail().equals(auth.getName());

        if (!isAdmin && !isOwner) {
            throw new IllegalStateException("Vous n'êtes pas autorisé à modifier ce bien");
        }

        validerNombrePieces(request.getCategorie(), request.getNombrePiecesDisponibles());

        // Nettoyage Cloudinary : supprimer les images retirées lors de la modification
        List<ImageBien> nouvellesSecondaires = toEntityImages(request.getPhotosSecondaires());
        supprimerImagesRetirees(bien, request.getImagePublicId(), nouvellesSecondaires);

        bien.setCategorie(request.getCategorie());
        bien.setNom(request.getNom());
        bien.setVille(request.getVille());
        bien.setQuartier(request.getQuartier());
        bien.setPhotoPrincipale(request.getPhotoPrincipale());
        bien.setImagePublicId(request.getImagePublicId());
        bien.setPhotosSecondaires(nouvellesSecondaires);
        bien.setPrixMensuel(request.getPrixMensuel());
        bien.setNombrePiecesDisponibles(request.getNombrePiecesDisponibles());
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

        // Supprimer d'abord toutes les images du bien sur Cloudinary (best-effort)
        imageStorageService.delete(bien.getImagePublicId());
        bien.getPhotosSecondaires().forEach(img -> imageStorageService.delete(img.getPublicId()));

        bienRepository.delete(bien);

        return MessageResponse.builder()
                .message("Bien supprimé avec succès")
                .build();
    }

    // ✅ Changer le statut d'un bien (appelé par reservation-service)
    @Transactional
    public BienResponse changerStatut(Long id, StatutUpdateRequest request) {
        Bien bien = bienRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Bien introuvable avec l'id : " + id));
        bien.setStatut(request.getStatut());
        return toResponse(bienRepository.save(bien));
    }

    // ✅ Filtrer par catégorie
    public List<BienResponse> getBiensByCategorie(String categorie) {
        Categorie cat = Categorie.valueOf(categorie.toUpperCase());
        return bienRepository.findByCategorie(cat).stream().map(this::toResponse).toList();
    }

    // ✅ Rechercher par ville ou quartier
    public List<BienResponse> rechercherParVilleOuQuartier(String ville, String quartier) {
        return bienRepository.findByVilleContainingIgnoreCaseOrQuartierContainingIgnoreCase(
                ville == null ? "" : ville,
                quartier == null ? "" : quartier
        ).stream().map(this::toResponse).toList();
    }

    // ✅ Mes biens (agent connecté) : filtré sur l'email du JWT, identifiant fiable du propriétaire
    public List<BienResponse> mesBiens(Authentication auth) {
        return bienRepository.findByAgentEmail(auth.getName()).stream().map(this::toResponse).toList();
    }

    // ✅ Prix de visite pour une catégorie (appelé par visite-service)
    public PrixVisiteResponse getPrixVisiteByCategorie(String categorie) {
        Categorie cat = Categorie.valueOf(categorie.toUpperCase());
        PrixVisite prixVisite = prixVisiteRepository.findByCategorie(cat)
                .orElseThrow(() -> new IllegalStateException("Aucun prix de visite configuré pour la catégorie : " + cat));
        return PrixVisiteResponse.builder()
                .categorie(prixVisite.getCategorie())
                .prixVisite(prixVisite.getPrixVisite())
                .build();
    }

    // ✅ Mise à jour du prix de visite d'une catégorie (ADMIN uniquement)
    @Transactional
    public PrixVisiteResponse updatePrixVisite(String categorie, PrixVisiteUpdateRequest request, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new IllegalStateException("Seul un administrateur peut modifier le prix de visite");
        }

        Categorie cat = Categorie.valueOf(categorie.toUpperCase());
        PrixVisite prixVisite = prixVisiteRepository.findByCategorie(cat)
                .orElseThrow(() -> new IllegalStateException("Aucun prix de visite configuré pour la catégorie : " + cat));
        prixVisite.setPrixVisite(request.getPrixVisite());
        prixVisiteRepository.save(prixVisite);

        return PrixVisiteResponse.builder()
                .categorie(prixVisite.getCategorie())
                .prixVisite(prixVisite.getPrixVisite())
                .build();
    }

    private void validerNombrePieces(Categorie categorie, Integer nombrePieces) {
        if (categorie == Categorie.CITE && nombrePieces == null) {
            throw new IllegalStateException("Le nombre de pièces disponibles est obligatoire pour une CITE");
        }
    }

    // Mapper entité → DTO
    private BienResponse toResponse(Bien bien) {
        return BienResponse.builder()
                .id(bien.getId())
                .categorie(bien.getCategorie())
                .nom(bien.getNom())
                .ville(bien.getVille())
                .quartier(bien.getQuartier())
                .photoPrincipale(bien.getPhotoPrincipale())
                .imagePublicId(bien.getImagePublicId())
                .photosSecondaires(toDtoImages(bien.getPhotosSecondaires()))
                .prixMensuel(bien.getPrixMensuel())
                .nombrePiecesDisponibles(bien.getNombrePiecesDisponibles())
                .date(bien.getDate())
                .description(bien.getDescription())
                .agentEmail(bien.getAgentEmail())
                .statut(bien.getStatut())
                .build();
    }

    // ---------- Gestion des images (Cloudinary) ----------

    // ✅ Upload d'une image vers Cloudinary (retourne secure_url + public_id)
    public ImageUploadResponse uploadImage(org.springframework.web.multipart.MultipartFile file) {
        return imageStorageService.upload(file);
    }

    // ✅ Suppression directe d'une image orpheline sur Cloudinary (image uploadée puis non utilisée)
    public MessageResponse supprimerImage(String publicId) {
        imageStorageService.delete(publicId);
        return MessageResponse.builder()
                .message("Image supprimée avec succès")
                .build();
    }

    // Supprime de Cloudinary les images présentes avant modification mais absentes après
    private void supprimerImagesRetirees(Bien ancien, String nouveauPublicIdPrincipale, List<ImageBien> nouvellesSecondaires) {
        // Photo principale remplacée
        String anciennePrincipale = ancien.getImagePublicId();
        if (anciennePrincipale != null && !anciennePrincipale.equals(nouveauPublicIdPrincipale)) {
            imageStorageService.delete(anciennePrincipale);
        }
        // Photos secondaires retirées
        List<String> conserves = nouvellesSecondaires.stream().map(ImageBien::getPublicId).toList();
        ancien.getPhotosSecondaires().stream()
                .map(ImageBien::getPublicId)
                .filter(pid -> pid != null && !conserves.contains(pid))
                .forEach(imageStorageService::delete);
    }

    private List<ImageBien> toEntityImages(List<ImageInfo> images) {
        if (images == null) {
            return new ArrayList<>();
        }
        return images.stream()
                .map(i -> ImageBien.builder().url(i.getUrl()).publicId(i.getPublicId()).build())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private List<ImageInfo> toDtoImages(List<ImageBien> images) {
        if (images == null) {
            return new ArrayList<>();
        }
        return images.stream()
                .map(i -> ImageInfo.builder().url(i.getUrl()).publicId(i.getPublicId()).build())
                .toList();
    }
}
