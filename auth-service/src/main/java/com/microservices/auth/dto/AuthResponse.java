package com.microservices.auth.dto;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    // --- Ajout des informations utilisateur demandées ---
    private String firstname;
    
    private String lastname;
    
    private String email;
    
    private Set<String> roles;
    
    // Le champ expiresIn a été supprimé !
}