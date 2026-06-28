package com.immo.messagerie.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {

    private Long conversationId;

    private Long receiverId;

    private Long bienId;

    @NotBlank(message = "Le contenu du message est obligatoire")
    private String contenu;
}
