package com.jobportal.backend.services;

import com.jobportal.backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    // ✅ Secure Key Extraction (Fix WeakKeyException)
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret); // Decode Base64 secret
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ✅ Generate JWT Token
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail()) // Email as subject
                .claim("role", user.getRole()) // Add role as claim
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token expires in 1 day
                .signWith(getSigningKey(), SignatureAlgorithm.HS512) // HS512 Signature
                .compact();
    }

    // ✅ Extract Email from Token
    public String extractEmail(String token) {
        return extractClaims(token).getSubject(); // Subject is the email
    }

    // ✅ Extract Role from Token
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class); // Get role from claims
    }

    // ✅ Extract Claims (Helper Method)
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public void invalidateToken(String token) {
        if(isTokenBlacklisted(token)) throw new RuntimeException("Token already expired.");
        blacklistedTokens.add(token);
    }
    // ✅ Check if Token is Valid
    public boolean validateToken(String token, User user) {
        try {
            String email = extractEmail(token);
            return (email.equals(user.getEmail()) && !isTokenExpired(token) && !blacklistedTokens.contains(token));
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    // ✅ Check if Token is Expired
    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
