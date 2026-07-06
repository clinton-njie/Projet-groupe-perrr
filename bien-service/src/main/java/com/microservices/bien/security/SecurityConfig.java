package com.microservices.bien.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Consulter les biens : tout le monde
                        .requestMatchers(HttpMethod.GET, "/api/biens/**").permitAll()
                        // Changement de statut : appel inter-service (reservation-service), pas d'auth utilisateur
                        .requestMatchers(HttpMethod.PUT, "/api/biens/*/statut").permitAll()
                        // Créer/Modifier/Supprimer : AGENT ou ADMIN uniquement
                        .requestMatchers(HttpMethod.POST, "/api/biens/**")
                            .hasAnyAuthority("ROLE_AGENT", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/biens/**")
                            .hasAnyAuthority("ROLE_AGENT", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/biens/**")
                            .hasAnyAuthority("ROLE_AGENT", "ROLE_ADMIN")
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
