package com.microservices.auth.dto;

import lombok.*;

// Payload envoyé à service-user pour créer le profil associé à l'utilisateur inscrit.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInitRequest {

    private Long authUserId;

    private String firstname;

    private String lastname;

    private String email;
}
