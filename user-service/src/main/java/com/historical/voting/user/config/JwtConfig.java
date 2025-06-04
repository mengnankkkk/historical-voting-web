package com.historical.voting.user.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtConfig {
    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    private final SecretKey accessTokenKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final SecretKey refreshTokenKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

    public String generateAccessToken(String username) {
        return generateToken(username, accessTokenExpiration, accessTokenKey);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenExpiration, refreshTokenKey);
    }

    private String generateToken(String username, long expiration, SecretKey key) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public Claims validateAccessToken(String token) {
        return validateToken(token, accessTokenKey);
    }

    public Claims validateRefreshToken(String token) {
        return validateToken(token, refreshTokenKey);
    }

    private Claims validateToken(String token, SecretKey key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
} 