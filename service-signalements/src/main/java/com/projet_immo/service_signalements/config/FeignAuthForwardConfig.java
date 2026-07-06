package com.projet_immo.service_signalements.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Propage le header Authorization de la requête entrante vers les appels Feign sortants.
 * Nécessaire pour le bannissement : user-service /internal/users/** exige ROLE_ADMIN, et
 * c'est justement un administrateur qui déclenche la sanction (PUT /signalements/{id}/statut).
 */
@Configuration
public class FeignAuthForwardConfig {

    @Bean
    public RequestInterceptor authForwardInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return;
            }
            HttpServletRequest request = attrs.getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization != null && !authorization.isBlank()) {
                template.header("Authorization", authorization);
            }
        };
    }
}
