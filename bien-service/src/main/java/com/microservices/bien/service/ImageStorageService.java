package com.microservices.bien.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.microservices.bien.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Stockage des images des biens sur Cloudinary (dossier "biens").
 * Remplace tout stockage local : le service ne touche jamais au disque.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImageStorageService {

    private static final String DOSSIER = "biens";

    private final Cloudinary cloudinary;

    /**
     * Upload un fichier vers Cloudinary dans le dossier "biens".
     * @return l'URL sécurisée (secure_url) et le publicId de l'image.
     */
    public ImageUploadResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("Aucun fichier fourni pour l'upload");
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", DOSSIER)
            );
            return ImageUploadResponse.builder()
                    .secureUrl((String) result.get("secure_url"))
                    .publicId((String) result.get("public_id"))
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Échec de l'upload de l'image vers Cloudinary", e);
        }
    }

    /**
     * Supprime une image de Cloudinary via son publicId.
     * En best-effort : on log l'erreur sans la propager, pour ne pas faire
     * échouer la suppression/modification d'un bien à cause de Cloudinary.
     */
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.warn("Impossible de supprimer l'image Cloudinary (publicId={}) : {}", publicId, e.getMessage());
        }
    }
}
