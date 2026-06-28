package com.immo.messagerie.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationRequest {

    @NotNull(message = "Le destinataire est obligatoire")
    private Long receiverId;

    @NotNull(message = "Le bien concerné est obligatoire")
    private Long bienId;
}
