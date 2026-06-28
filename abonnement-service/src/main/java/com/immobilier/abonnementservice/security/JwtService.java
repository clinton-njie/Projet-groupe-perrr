package com.immobilier.abonnementservice.security;

import com.immobilier.abonnementservice.dto.response.UtilisateurConnecteResponse;
import com.immobilier.abonnementservice.feign.AuthFeignClient;
import com.immobilier.abonnementservice.exception.AbonnementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final AuthFeignClient authFeignClient;

    public UtilisateurConnecteResponse getUser(String token) {

        UtilisateurConnecteResponse user =
                authFeignClient.getUtilisateurConnecte(token);

        if (user == null) {
            throw new AbonnementException("Utilisateur non authentifié");
        }

        return user;
    }

    public Long getUserId(String token) {
        return getUser(token).getId();
    }
}