package com.example.bookingsystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JWTUtil {

    private final Key signingKey;
    private final long expirationMillis;

    public JWTUtil(
           
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms:3600000}") long expirationMillis) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(username)
                .claim("roles", List.of(role))
                .issuedAt(now)
                .expiration(expiry)
                .signWith((SecretKey) signingKey)
                .compact();
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> (List<String>) claims.get("roles"));
    }

    public boolean isTokenValid(String token, String usernameFromContext) {
        try {
            String usernameInToken = extractUsername(token);
            return usernameInToken.equals(usernameFromContext) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // Still return claims so callers can distinguish "expired" from "malformed".
            return e.getClaims();
        }
    }
}
