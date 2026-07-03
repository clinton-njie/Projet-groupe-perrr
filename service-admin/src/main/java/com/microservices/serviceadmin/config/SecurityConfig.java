package com.microservices.serviceadmin.config;

import com.microservices.serviceadmin.security.JwtAuthenticationFilter;
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

// Un anonyme suit un lien d'invitation reçu par email : pas de JWT à ce stade.
.requestMatchers(HttpMethod.GET, "/admin/invites/*/validate").permitAll()
.requestMatchers(HttpMethod.POST, "/admin/invites/*/accept").permitAll()

// Appelé par service-user quand un client soumet une demande (pas de contexte JWT admin).
.requestMatchers(HttpMethod.POST, "/internal/role-requests").permitAll()

// Seul un super admin peut inviter un nouvel admin.
.requestMatchers(HttpMethod.POST, "/admin/invites").hasAuthority("ROLE_SUPER_ADMIN")

// Reste des endpoints /admin/** : admin ou super admin.
.requestMatchers("/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")

.anyRequest().authenticated())
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
.build();
}
}
