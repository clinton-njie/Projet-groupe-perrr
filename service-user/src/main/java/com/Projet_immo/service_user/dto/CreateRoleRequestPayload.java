package com.Projet_immo.service_user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoleRequestPayload {
    private Long authUserId;
    private String currentRole;
    private String requestedRole;
    private String justification;
}
