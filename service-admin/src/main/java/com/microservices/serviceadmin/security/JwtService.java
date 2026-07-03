package com.microservices.serviceadmin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtService {

@Value("${jwt.secret}")
private String jwtSecret;

public String extractEmail(String token) {
return extractAllClaims(token).getSubject();
}

@SuppressWarnings("unchecked")
public List<String> extractRoles(String token) {
return (List<String>) extractAllClaims(token).get("roles");
}

public boolean isTokenExpired(String token) {
return extractAllClaims(token).getExpiration().before(new Date());
}

public boolean isTokenValid(String token) {
try {
return !isTokenExpired(token);
} catch (Exception e) {
return false;
}
}

private Claims extractAllClaims(String token) {
return Jwts.parser()
.verifyWith(getSigningKey())
.build()
.parseSignedClaims(token)
.getPayload();
}

private SecretKey getSigningKey() {
byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
return Keys.hmacShaKeyFor(keyBytes);
}
}
