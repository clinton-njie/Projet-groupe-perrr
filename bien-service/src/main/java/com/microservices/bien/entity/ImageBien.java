package com.microservices.bien.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * Une image d'un bien telle que renvoyée par Cloudinary.
 * On conserve l'URL affichable ET le publicId, indispensable pour supprimer
 * l'image de Cloudinary plus tard (éviter les images orphelines).
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageBien {

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "public_id", length = 255)
    private String publicId;
}
