package com.microservices.serviceadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InviteResponse {
private String message;
// Le lien serait normalement envoyé par email uniquement ; il est aussi renvoyé ici
// car l'envoi d'email est stubé (pas de SMTP configuré pour l'instant).
private String inviteLink;
}
