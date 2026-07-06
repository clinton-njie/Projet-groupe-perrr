package com.microservices.bien.dto;

import lombok.*;

/**
 * Réponse de l'endpoint d'upload : le client reçoit l'URL à afficher/enregistrer
 * et le publicId à renvoyer plus tard pour la suppression.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadResponse {
    private String secureUrl;
    private String publicId;
}
