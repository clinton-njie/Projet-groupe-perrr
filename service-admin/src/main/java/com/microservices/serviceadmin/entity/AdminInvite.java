package com.microservices.serviceadmin.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_invites")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminInvite {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false, unique = true)
private String token;

@Column(nullable = false)
private String email;

@Column(nullable = false)
private String role;

@Column(nullable = false)
private LocalDateTime expiresAt;

@Builder.Default
@Column(nullable = false)
private Boolean used = false;

@Column(nullable = false)
private LocalDateTime createdAt;
}
