package com.projet_immo.service_signalements.config;

import com.projet_immo.service_signalements.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Remplace @RequiredArgsConstructor par un constructeur explicite
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Le dispatch interne vers /error ne rejoue pas le filtre JWT (OncePerRequestFilter
                        // saute les ERROR dispatch) : sans ceci, tout ResponseStatusException lancé par un
                        // contrôleur est masqué en 403 au lieu de son vrai statut (409, 400, ...).
                        .requestMatchers("/error").permitAll()

                        // Objets de signalement : consultation ouverte aux utilisateurs authentifiés,
                        // création/modification/désactivation réservées aux administrateurs.
                        // (Placé AVANT les règles /signalements/* pour éviter que /signalements/objets
                        // ne soit capturé par le matcher générique à un segment.)
                        .requestMatchers(HttpMethod.GET, "/signalements/objets", "/signalements/objets/**")
                        .hasAnyAuthority("ROLE_CLIENT", "ROLE_AGENT", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/signalements/objets").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/signalements/objets/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                        // ADMIN et SUPER_ADMIN uniquement
                        .requestMatchers(HttpMethod.GET, "/signalements").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/signalements/*/statut").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                        // CLIENT et AGENT uniquement
                        .requestMatchers(HttpMethod.POST, "/signalements").hasAnyAuthority("ROLE_CLIENT", "ROLE_AGENT")

                        // CLIENT, AGENT et ADMIN
                        .requestMatchers(HttpMethod.GET, "/signalements/*")
                        .hasAnyAuthority("ROLE_CLIENT", "ROLE_AGENT", "ROLE_ADMIN", "ROLE_SUPER_ADMIN")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}