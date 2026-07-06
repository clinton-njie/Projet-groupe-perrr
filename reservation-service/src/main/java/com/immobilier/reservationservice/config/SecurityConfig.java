package com.immobilier.reservationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Même secret qu'auth-service ; auth-service dérive sa clé via jjwt's
    // Keys.hmacShaKeyFor(secret.getBytes(UTF_8)), qui choisit HS512 pour un
    // secret de cette longueur (73 octets >= 64 -> HS512, pas HS256). Il n'existe
    // pas de propriété Spring Boot standard "secret-key" pour l'auto-configuration
    // HS* du resource server : sans ce bean explicite, aucun JwtDecoder n'est créé
    // et le contexte Spring échoue au démarrage.
    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String jwtSecret;

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS512).build();
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // Callbacks inter-services (payment-service) : pas de JWT utilisateur disponible
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/reservations/*/confirmer").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/reservations/*/annuler").permitAll()
                        // Vérification d'interaction inter-services (service-signalements) : appel Feign sans JWT
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/reservations/verification").permitAll()
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth ->
                        oauth.jwt(Customizer.withDefaults()));

        return http.build();
    }

}
