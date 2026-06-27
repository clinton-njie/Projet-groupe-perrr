package com.immo.messagerie.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String contenu;
    private Boolean isRead;
    private Instant readAt;
    private Instant createdAt;
}
