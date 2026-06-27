package com.immo.messagerie.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private Long bienId;
    private String lastMessage;
    private Instant lastMessageAt;
    private Instant createdAt;
    private long unreadCount;
}
