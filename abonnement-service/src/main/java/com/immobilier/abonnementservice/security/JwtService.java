package com.immobilier.abonnementservice.security;

import com.immobilier.abonnementservice.dto.response.UtilisateurConnecteResponse;
import com.immobilier.abonnementservice.exception.AbonnementException;
import com.immobilier.abonnementservice.feign.UserFeignClient;
import com.immobilier.abonnementservice.feign.dto.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// Résout l'utilisateur connecté : email + rôle depuis le JWT, puis identité canonique
// (id, nom, prénom) via user-service (on ne fait jamais confiance au JWT seul).
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;
    private final UserFeignClient userFeignClient;

    public UtilisateurConnecteResponse getUser(String authHeader) {
        String token = JwtUtil.clean(authHeader);
        if (token == null || !jwtUtil.isValid(token)) {
            throw new AbonnementException("Utilisateur non authentifié");
        }

        String email = jwtUtil.extractEmail(token);
        String role = roleMetier(jwtUtil.extractRoles(token)); // "AGENT", "ADMIN", ...

        Long id = userFeignClient.findIdByEmail(email);
        if (id == null) {
            throw new AbonnementException("Utilisateur introuvable dans user-service : " + email);
        }
        UserProfileDto profil = userFeignClient.findProfileById(id);

        UtilisateurConnecteResponse user = new UtilisateurConnecteResponse();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        if (profil != null) {
            user.setNom(profil.getLastname());
            user.setPrenom(profil.getFirstname());
            user.setActif(profil.getIsActive() == null || profil.getIsActive());
        } else {
            user.setActif(true);
        }
        return user;
    }

    // Le rôle métier attendu par la spec est sans préfixe (AGENT, ADMIN).
    private String roleMetier(List<String> roles) {
        if (roles == null || roles.isEmpty()) return null;
        String r = roles.get(0);
        return r.startsWith("ROLE_") ? r.substring(5) : r;
    }
}
