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

        // ADMIN uniquement (Consultation et mise a jour d'un signalement)
        .requestMatchers(HttpMethod.GET, "/api/signalements").hasAuthority("ROLE_ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/signalements/*/statut").hasAuthority("ROLE_ADMIN")
        .requestMatchers(HttpMethod.GET, "/api/signalements/*").hasAnyAuthority("ROLE_ADMIN")

        // USER et AGENT uniquement (Ajout d'un signalement)
        .requestMatchers(HttpMethod.POST, "/api/signalements/agent").hasAnyAuthority("ROLE_CLIENT", "ROLE_AGENT")
        .requestMatchers(HttpMethod.POST, "/api/signalements/technique").hasAnyAuthority("ROLE_CLIENT", "ROLE_AGENT")

        // 🔐 OBJET SIGNALEMENT - CRUD ADMIN ONLY
        .requestMatchers(HttpMethod.POST, "/api/objets-signalement").hasAuthority("ROLE_ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/objets-signalement/*").hasAuthority("ROLE_ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/api/objets-signalement/*").hasAuthority("ROLE_ADMIN")

        // consultation des objets (USER + AGENT + ADMIN)
        .requestMatchers(HttpMethod.GET, "/api/objets-signalement").hasAnyAuthority("ROLE_ADMIN", "ROLE_AGENT", "ROLE_CLIENT")
        .requestMatchers(HttpMethod.GET, "/api/objets-signalement/*").hasAnyAuthority("ROLE_ADMIN", "ROLE_AGENT", "ROLE_CLIENT")

        .anyRequest().authenticated()
)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}