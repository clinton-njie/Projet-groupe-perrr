package com.immobilier.reservationservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// Propage le header Authorization de la requête entrante vers les appels Feign sortants
// (UserClient, PropertyClient, PaymentClient) : ces services vérifient le JWT côté serveur,
// sans ça leurs endpoints authentifiés répondent 403 aux appels inter-services.
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor authForwardingInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader != null) {
                requestTemplate.header("Authorization", authHeader);
            }
        };
    }
}