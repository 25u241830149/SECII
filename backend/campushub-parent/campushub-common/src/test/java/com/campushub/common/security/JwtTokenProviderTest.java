package com.campushub.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campushub.common.config.JwtConfig;
import com.campushub.common.enums.UserRole;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        JwtConfig config = new JwtConfig();
        config.setSecret("test-secret-key-for-unit-tests-32bytes");
        config.setAccessTokenTtl(Duration.ofHours(2));
        config.setRefreshTokenTtl(Duration.ofDays(7));
        config.setIssuer("campushub-test");
        config.setHeaderName("Authorization");
        config.setTokenPrefix("Bearer ");
        tokenProvider = new JwtTokenProvider(config);
    }

    @Test
    void generateAccessTokenContainsStandardClaims() {
        String token = tokenProvider.generateAccessToken(7L, "alice", UserRole.USER);

        assertNotNull(token);
        assertTrue(token.length() > 20);
    }

    @Test
    void generateRefreshTokenProducesLongerLivedToken() {
        String accessToken = tokenProvider.generateAccessToken(7L, "alice", UserRole.USER);
        String refreshToken = tokenProvider.generateRefreshToken(7L, "alice", UserRole.USER);

        assertTrue(tokenProvider.getExpiresAt(refreshToken)
                .isAfter(tokenProvider.getExpiresAt(accessToken)));
    }

    @Test
    void validateTokenReturnsTrueForValidToken() {
        String token = tokenProvider.generateAccessToken(7L, "alice", UserRole.USER);

        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void validateTokenReturnsFalseForTamperedToken() {
        String token = tokenProvider.generateAccessToken(7L, "alice", UserRole.USER);

        // Tamper with token: change last character
        String tampered = token.substring(0, token.length() - 1)
                + (token.charAt(token.length() - 1) == 'A' ? 'B' : 'A');

        assertFalse(tokenProvider.validateToken(tampered));
    }

    @Test
    void validateTokenReturnsFalseForGarbage() {
        assertFalse(tokenProvider.validateToken("garbage"));
        assertFalse(tokenProvider.validateToken(""));
    }

    @Test
    void getUserIdExtractsCorrectValue() {
        String token = tokenProvider.generateAccessToken(7L, "alice", UserRole.USER);

        assertEquals(7L, tokenProvider.getUserId(token));
    }

    @Test
    void getUsernameExtractsCorrectValue() {
        String token = tokenProvider.generateAccessToken(7L, "alice", UserRole.USER);

        assertEquals("alice", tokenProvider.getUsername(token));
    }

    @Test
    void getRoleExtractsCorrectValue() {
        String token = tokenProvider.generateAccessToken(7L, "alice", UserRole.ADMIN);

        assertEquals(UserRole.ADMIN, tokenProvider.getRole(token));
    }

    @Test
    void getExpiresAtReturnsFutureInstant() {
        String token = tokenProvider.generateAccessToken(7L, "alice", UserRole.USER);

        assertTrue(tokenProvider.getExpiresAt(token).isAfter(java.time.Instant.now()));
    }

    @Test
    void resolveTokenExtractsFromBearerHeader() {
        String tokenValue = tokenProvider.generateAccessToken(7L, "alice", UserRole.USER);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenValue);

        assertEquals(tokenValue, tokenProvider.resolveToken(request));
    }

    @Test
    void resolveTokenReturnsNullForMissingHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertNull(tokenProvider.resolveToken(request));
    }

    @Test
    void resolveTokenReturnsNullForWrongPrefix() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic xyz");

        assertNull(tokenProvider.resolveToken(request));
    }

    @Test
    void resolveTokenReturnsNullForBlankTokenAfterPrefix() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer   ");

        assertNull(tokenProvider.resolveToken(request));
    }

    @Test
    void getUserIdThrowsOnUnknownToken() {
        assertThrows(RuntimeException.class, () -> tokenProvider.getUserId("garbage.invalid.token"));
    }

    @Test
    void getRoleThrowsOnUnknownToken() {
        assertThrows(RuntimeException.class, () -> tokenProvider.getRole("garbage.invalid.token"));
    }
}
