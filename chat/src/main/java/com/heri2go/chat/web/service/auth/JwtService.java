package com.heri2go.chat.web.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final SecretKey secretKey;

    public String generateAccessToken(String username) {
        return getDefaultBuilder(username)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 minutes
                .compact();
    }

    public String generateRefreshToken(String username) {
        return getDefaultBuilder(username)
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 14)) // 14 days
                .compact();
    }

    private JwtBuilder getDefaultBuilder(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(secretKey, SignatureAlgorithm.HS256);
    }

    public boolean validateSubject(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
