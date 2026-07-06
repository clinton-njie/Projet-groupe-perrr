package com.microservices.bien.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Une image d'un bien côté API : le couple { url, publicId } retourné par
 * l'endpoint d'upload et renvoyé tel quel à la création/modification du bien.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageInfo {

    @NotBlank(message = "L'URL de l'image est obligatoire")
    private String url;

    @NotBlank(message = "Le publicId de l'image est obligatoire")
    private String publicId;
}
