package com.jobplatform.security;

import com.jobplatform.config.JwtConfig;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTests {

    private JwtService jwtService;
    private UserDetails userDetails;
    private static final String TEST_SECRET = "test-secret-key-for-testing-only-change-in-production";
    private static final long TEST_EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        JwtConfig jwtConfig = new JwtConfig();
        jwtConfig.setSecret(TEST_SECRET);
        jwtConfig.setExpirationMs(TEST_EXPIRATION);
        jwtService = new JwtService(jwtConfig);

        userDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_returnsNonEmptyToken() {
        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_containsCorrectSubject() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("test@example.com");
    }

    @Test
    void generateToken_withExtraClaims_containsClaims() {
        String token = jwtService.generateToken(
                Collections.singletonMap("role", UserRole.CANDIDATE.name()), userDetails);
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        assertThat(role).isEqualTo("CANDIDATE");
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        JwtConfig shortLivedConfig = new JwtConfig();
        shortLivedConfig.setSecret(TEST_SECRET);
        shortLivedConfig.setExpirationMs(0);
        JwtService shortLivedService = new JwtService(shortLivedConfig);

        String token = shortLivedService.generateToken(userDetails);
        assertThat(shortLivedService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_differentUser_returnsFalse() {
        String token = jwtService.generateToken(userDetails);
        UserDetails otherUser = User.builder()
                .username("other@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValid_noArg_validToken_returnsTrue() {
        String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_noArg_invalidToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("invalid.token.here")).isFalse();
    }

    @Test
    void isTokenValid_noArg_emptyToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("")).isFalse();
    }

    @Test
    void extractExpiration_returnsDate() {
        String token = jwtService.generateToken(userDetails);
        assertThat(jwtService.extractExpiration(token)).isNotNull();
    }

    @Test
    void getExpirationMs_returnsConfigValue() {
        assertThat(jwtService.getExpirationMs()).isEqualTo(TEST_EXPIRATION);
    }
}
