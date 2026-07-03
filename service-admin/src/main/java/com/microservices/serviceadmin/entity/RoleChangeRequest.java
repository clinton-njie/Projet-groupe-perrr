package com.microservices.serviceadmin.entity;

import com.microservices.serviceadmin.enums.RoleRequestStatut;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_change_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleChangeRequest {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false)
private Long authUserId;

// "current_role" est un mot réservé PostgreSQL (fonction système CURRENT_ROLE) -> colonne renommée.
@Column(name = "current_role_value", nullable = false)
private String currentRole;

@Column(nullable = false)
private String requestedRole;

@Column(length = 500)
private String justification;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private RoleRequestStatut statut;

@Column(nullable = false)
private LocalDateTime createdAt;

private LocalDateTime processedAt;

private String refusalReason;
}
