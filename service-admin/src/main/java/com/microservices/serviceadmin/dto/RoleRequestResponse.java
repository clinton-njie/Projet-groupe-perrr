package com.microservices.serviceadmin.dto;

import com.microservices.serviceadmin.enums.RoleRequestStatut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestResponse {
private Long id;
private Long authUserId;
private String currentRole;
private String requestedRole;
private String justification;
private RoleRequestStatut statut;
private LocalDateTime createdAt;
private LocalDateTime processedAt;
private String refusalReason;
}
