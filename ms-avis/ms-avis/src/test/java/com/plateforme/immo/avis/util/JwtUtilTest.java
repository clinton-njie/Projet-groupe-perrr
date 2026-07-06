package com.plateforme.immo.avis.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    // Même secret que application.yml (profil par défaut) — 64 octets, OK pour HS256.
    private static final String SECRET =
            "9a4f434556515d736178274a4dd4e635252d624757662c6563242f362e2a4a51";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
    }

    // Reproduit le token émis par auth-service (subject = email, claim "roles" = liste).
    private String token(String email, List<String> roles) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }

    @Test
    void extractEmail_retourneLeSubject() {
        String t = token("client@test.com", List.of("ROLE_CLIENT"));
        assertEquals("client@test.com", jwtUtil.extractEmail(t));
    }

    @Test
    void extractRoles_retourneLaListeDuClaimRoles() {
        String t = token("client@test.com", List.of("ROLE_CLIENT", "ROLE_AGENT"));
        assertEquals(List.of("ROLE_CLIENT", "ROLE_AGENT"), jwtUtil.extractRoles(t));
    }

    @Test
    void extractRoles_retourneVideQuandClaimAbsent() {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String t = Jwts.builder().setSubject("x@test.com").signWith(key).compact();
        assertTrue(jwtUtil.extractRoles(t).isEmpty());
    }

    @Test
    void hasRole_vraiSiPresentFauxSinon() {
        String t = token("client@test.com", List.of("ROLE_CLIENT"));
        assertTrue(jwtUtil.hasRole(t, "ROLE_CLIENT"));
        assertFalse(jwtUtil.hasRole(t, "ROLE_AGENT"));
    }
}
