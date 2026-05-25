package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // 1. SECRET KEY - Must be consistent across your services
    private static final String SECRET = "mysecretkeymysecretkeymysecretkey123456";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 2. GENERATE TOKEN (Used by AuthController during Login)
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        // IMPORTANT: Add the role here so the filter can read it later!
        // If your user is an admin, set this to "ADMIN"
        claims.put("role", "ADMIN"); 

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // 3. VALIDATE TOKEN (Used by JwtAuthenticationFilter)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // 4. EXTRACT DATA (Used by JwtAuthenticationFilter)
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("role");
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}