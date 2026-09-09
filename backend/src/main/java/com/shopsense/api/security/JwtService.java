package com.shopsense.api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMinutes;
    public JwtService(@Value("${shopsense.jwt.secret}") String secret, @Value("${shopsense.jwt.expiration-minutes}") long expirationMinutes) {
        if (secret.length() < 32) throw new IllegalArgumentException("JWT secret must be at least 32 characters");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }
    public String createToken(String subject, String role) {
        Instant now = Instant.now();
        return Jwts.builder().subject(subject).claim("role", role).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirationMinutes * 60))).signWith(key).compact();
    }
    public String subject(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject(); }
    public String role(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role", String.class); }
}
