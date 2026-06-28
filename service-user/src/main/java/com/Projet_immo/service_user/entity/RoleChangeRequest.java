package com.Projet_immo.service_user.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "role_change_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long authUserId;

    @Column(name = "current_user_role", nullable = false)
    private String currentRole;

    @Column(nullable = false)
    private String requestedRole;

    @Column(length = 500)
    private String justification;

    @Builder.Default
    @Column(nullable = false)
    private String status = "EN_ATTENTE";

    @Builder.Default
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant processedAt;

    private Long processedBy;

    private String refusalReason;
}
