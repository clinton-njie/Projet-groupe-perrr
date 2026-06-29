package com.immobilier.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// Configuration de sécurité pour l'application, 
// définissant les règles d'accès et les mécanismes d'authentification
@Configuration
public class SecurityConfig {

    // Bean pour la chaîne de filtres de sécurité, configurant les règles d'accès 
    // et désactivant CSRF pour les API REST
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}