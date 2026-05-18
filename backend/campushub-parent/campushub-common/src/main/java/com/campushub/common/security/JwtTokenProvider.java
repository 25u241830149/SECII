package com.campushub.common.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.campushub.common.config.JwtConfig;
import com.campushub.common.constant.ErrorCode;
import com.campushub.common.enums.UserRole;
import com.campushub.common.exception.BusinessException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtTokenProvider {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLE = "role";

    private final JwtConfig jwtConfig;
    private final SecretKey signingKey;

    public JwtTokenProvider(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        this.signingKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String username, UserRole role) {
        return generateToken(userId, username, role, jwtConfig.getAccessTokenTtl().toSeconds());
    }

    public String generateRefreshToken(Long userId, String username, UserRole role) {
        return generateToken(userId, username, role, jwtConfig.getRefreshTokenTtl().toSeconds());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Object value = parseClaims(token).get(CLAIM_USER_ID);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.valueOf(text);
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token 缺少用户 ID");
    }

    public String getUsername(String token) {
        return parseClaims(token).get(CLAIM_USERNAME, String.class);
    }

    public UserRole getRole(String token) {
        String role = parseClaims(token).get(CLAIM_ROLE, String.class);
        if (role == null || role.isBlank()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token 缺少用户角色");
        }
        return UserRole.valueOf(role);
    }

    public Instant getExpiresAt(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    public String resolveToken(HttpServletRequest request) {
        String headerValue = request.getHeader(jwtConfig.getHeaderName());
        String tokenPrefix = jwtConfig.getTokenPrefix();
        if (headerValue == null || tokenPrefix == null) {
            return null;
        }
        if (!headerValue.startsWith(tokenPrefix)) {
            return null;
        }
        String token = headerValue.substring(tokenPrefix.length()).trim();
        return token.isBlank() ? null : token;
    }

    private String generateToken(Long userId, String username, UserRole role, long ttlSeconds) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(ttlSeconds);
        Map<String, Object> claims = Map.of(
                CLAIM_USER_ID, userId,
                CLAIM_USERNAME, username,
                CLAIM_ROLE, role.name()
        );

        return Jwts.builder()
                .issuer(jwtConfig.getIssuer())
                .subject(String.valueOf(userId))
                .claims(claims)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtConfig.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
