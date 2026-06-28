package com.immo.messagerie.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "conversations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Conversation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "sender_id", nullable = false)
    private Long senderId;
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;
    @Column(name = "bien_id", nullable = false)
    private Long bienId;
    @Column(name = "last_message")
    private String lastMessage;
    @Column(name = "last_message_at")
    private Instant lastMessageAt;
    @Column(name = "is_deleted") @Builder.Default
    private Boolean isDeleted = false;
    @Column(name = "deleted_at")
    private Instant deletedAt;
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }
}
