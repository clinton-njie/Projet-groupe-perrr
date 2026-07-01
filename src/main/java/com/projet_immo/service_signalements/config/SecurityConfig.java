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

                        // ADMIN uniquement
                        .requestMatchers(HttpMethod.GET, "/signalements").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/signalements/*/statut").hasAuthority("ROLE_ADMIN")

                        // USER et AGENT uniquement
                        .requestMatchers(HttpMethod.POST, "/signalements").hasAnyAuthority("ROLE_CLIENT", "ROLE_AGENT")

                        // USER, AGENT et ADMIN
                        .requestMatchers(HttpMethod.GET, "/signalements/*")
                        .hasAnyAuthority("ROLE_CLIENT", "ROLE_AGENT", "ROLE_ADMIN")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}