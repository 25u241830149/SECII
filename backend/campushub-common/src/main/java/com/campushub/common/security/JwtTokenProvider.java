package com.campushub.common.security;

import com.campushub.common.config.JwtConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    public String createToken(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(padSecret(jwtConfig.getSecret()).getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + jwtConfig.getExpiration());
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expireAt)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        return token != null && !token.isBlank();
    }

    private String padSecret(String secret) {
        if (secret.length() >= 32) {
            return secret;
        }
        return String.format("%-32s", secret).replace(' ', '0');
    }
}