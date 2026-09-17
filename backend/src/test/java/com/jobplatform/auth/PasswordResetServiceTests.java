package com.jobplatform.auth;

import com.jobplatform.auth.dto.ChangePasswordRequest;
import com.jobplatform.auth.dto.ForgotPasswordRequest;
import com.jobplatform.auth.dto.ResetPasswordRequest;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRepository;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PasswordResetServiceTests {

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .fullName("Test User")
                .email("test-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CANDIDATE)
                .enabled(true)
                .build();
        userRepository.save(testUser);
    }

    private String hashToken(String token) throws Exception {
        Method method = PasswordResetService.class.getDeclaredMethod("hashToken", String.class);
        method.setAccessible(true);
        return (String) method.invoke(passwordResetService, token);
    }

    private PasswordResetToken createTokenForUser(User user, String rawToken) throws Exception {
        String tokenHash = hashToken(rawToken);
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
        return tokenRepository.save(token);
    }

    // ========== CHANGE PASSWORD TESTS ==========

    @Test
    void changePassword_validInputs_succeeds() throws Exception {
        passwordResetService.changePassword(
                testUser.getId(), "password123", "newPassword456", "newPassword456");

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword456", updated.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("password123", updated.getPassword())).isFalse();
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsBadRequest() {
        assertThatThrownBy(() -> passwordResetService.changePassword(
                testUser.getId(), "wrongPassword", "newPassword456", "newPassword456"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password is incorrect");
    }

    @Test
    void changePassword_passwordsDoNotMatch_throwsBadRequest() {
        assertThatThrownBy(() -> passwordResetService.changePassword(
                testUser.getId(), "password123", "newPassword456", "differentPassword"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Passwords do not match");
    }

    @Test
    void changePassword_tooShort_throwsBadRequest() {
        assertThatThrownBy(() -> passwordResetService.changePassword(
                testUser.getId(), "password123", "short", "short"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least 8 characters");
    }

    @Test
    void changePassword_tooLong_throwsBadRequest() {
        String longPassword = "a".repeat(129);
        assertThatThrownBy(() -> passwordResetService.changePassword(
                testUser.getId(), "password123", longPassword, longPassword))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must not exceed 128 characters");
    }

    @Test
    void changePassword_nonExistentUser_throwsNotFound() {
        assertThatThrownBy(() -> passwordResetService.changePassword(
                999999L, "password123", "newPassword456", "newPassword456"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changePassword_minimumLengthPassword_succeeds() throws Exception {
        String minPassword = "a".repeat(8);
        passwordResetService.changePassword(
                testUser.getId(), "password123", minPassword, minPassword);

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(minPassword, updated.getPassword())).isTrue();
    }

    @Test
    void changePassword_maximumLengthPassword_succeeds() throws Exception {
        String maxPassword = "a".repeat(128);
        passwordResetService.changePassword(
                testUser.getId(), "password123", maxPassword, maxPassword);

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(maxPassword, updated.getPassword())).isTrue();
    }

    // ========== FORGOT PASSWORD TESTS ==========

    @Test
    void forgotPassword_existingUser_createsToken() {
        passwordResetService.forgotPassword(testUser.getEmail());

        assertThat(tokenRepository.findByTokenHash("nonexistent")).isEmpty();
        var tokens = tokenRepository.findAll();
        assertThat(tokens).isNotEmpty();
        assertThat(tokens.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void forgotPassword_existingUser_deletesOldTokens() {
        passwordResetService.forgotPassword(testUser.getEmail());
        passwordResetService.forgotPassword(testUser.getEmail());

        var tokens = tokenRepository.findAll();
        long userTokenCount = tokens.stream()
                .filter(t -> t.getUser().getId().equals(testUser.getId()))
                .count();
        assertThat(userTokenCount).isEqualTo(1);
    }

    @Test
    void forgotPassword_nonExistentEmail_doesNotThrow() {
        passwordResetService.forgotPassword("nonexistent@example.com");

        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    void forgotPassword_tokenHasCorrectExpiry() {
        passwordResetService.forgotPassword(testUser.getEmail());

        var tokens = tokenRepository.findAll();
        assertThat(tokens).isNotEmpty();
        PasswordResetToken token = tokens.get(0);
        assertThat(token.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(token.getExpiresAt()).isBeforeOrEqualTo(LocalDateTime.now().plusMinutes(31));
    }

    @Test
    void forgotPassword_tokenIsNotUsed() {
        passwordResetService.forgotPassword(testUser.getEmail());

        var tokens = tokenRepository.findAll();
        assertThat(tokens).isNotEmpty();
        assertThat(tokens.get(0).getUsed()).isFalse();
    }

    // ========== RESET PASSWORD TESTS ==========

    @Test
    void resetPassword_validToken_succeeds() throws Exception {
        String rawToken = "valid-test-token";
        createTokenForUser(testUser, rawToken);

        passwordResetService.resetPassword(rawToken, "resetPassword789", "resetPassword789");

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("resetPassword789", updated.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("password123", updated.getPassword())).isFalse();
    }

    @Test
    void resetPassword_validToken_marksAsUsed() throws Exception {
        String rawToken = "used-test-token";
        PasswordResetToken savedToken = createTokenForUser(testUser, rawToken);

        passwordResetService.resetPassword(rawToken, "resetPassword789", "resetPassword789");

        PasswordResetToken updatedToken = tokenRepository.findById(savedToken.getId()).orElseThrow();
        assertThat(updatedToken.getUsed()).isTrue();
        assertThat(updatedToken.getUsedAt()).isNotNull();
    }

    @Test
    void resetPassword_invalidToken_throwsBadRequest() {
        assertThatThrownBy(() -> passwordResetService.resetPassword(
                "invalid-token", "resetPassword789", "resetPassword789"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid or expired reset token");
    }

    @Test
    void resetPassword_expiredToken_throwsBadRequest() throws Exception {
        String rawToken = "expired-test-token";
        String tokenHash = hashToken(rawToken);
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .user(testUser)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();
        tokenRepository.save(expiredToken);

        assertThatThrownBy(() -> passwordResetService.resetPassword(
                rawToken, "resetPassword789", "resetPassword789"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired");
    }

    @Test
    void resetPassword_alreadyUsedToken_throwsBadRequest() throws Exception {
        String rawToken = "already-used-token";
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .user(testUser)
                .tokenHash(hashToken(rawToken))
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(true)
                .usedAt(LocalDateTime.now().minusMinutes(5))
                .build();
        tokenRepository.save(usedToken);

        assertThatThrownBy(() -> passwordResetService.resetPassword(
                rawToken, "resetPassword789", "resetPassword789"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already been used");
    }

    @Test
    void resetPassword_passwordsDoNotMatch_throwsBadRequest() throws Exception {
        String rawToken = "mismatch-test-token";
        createTokenForUser(testUser, rawToken);

        assertThatThrownBy(() -> passwordResetService.resetPassword(
                rawToken, "resetPassword789", "differentPassword789"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Passwords do not match");
    }

    @Test
    void resetPassword_tooShort_throwsBadRequest() throws Exception {
        String rawToken = "short-pw-test-token";
        createTokenForUser(testUser, rawToken);

        assertThatThrownBy(() -> passwordResetService.resetPassword(
                rawToken, "short", "short"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least 8 characters");
    }

    @Test
    void resetPassword_tooLong_throwsBadRequest() throws Exception {
        String rawToken = "long-pw-test-token";
        createTokenForUser(testUser, rawToken);
        String longPassword = "a".repeat(129);

        assertThatThrownBy(() -> passwordResetService.resetPassword(
                rawToken, longPassword, longPassword))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must not exceed 128 characters");
    }

    @Test
    void resetPassword_subsequentLoginWorks() throws Exception {
        String rawToken = "login-test-token";
        createTokenForUser(testUser, rawToken);

        passwordResetService.resetPassword(rawToken, "newSecurePass123", "newSecurePass123");

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newSecurePass123", updated.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("password123", updated.getPassword())).isFalse();
    }

    // ========== PASSWORD RESET TOKEN ENTITY TESTS ==========

    @Test
    void token_isExpired_trueWhenPastExpiry() {
        PasswordResetToken token = PasswordResetToken.builder()
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();
        assertThat(token.isExpired()).isTrue();
    }

    @Test
    void token_isExpired_falseWhenNotPastExpiry() {
        PasswordResetToken token = PasswordResetToken.builder()
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
        assertThat(token.isExpired()).isFalse();
    }

    @Test
    void token_isValid_trueWhenNotUsedAndNotExpired() {
        PasswordResetToken token = PasswordResetToken.builder()
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
        assertThat(token.isValid()).isTrue();
    }

    @Test
    void token_isValid_falseWhenUsed() {
        PasswordResetToken token = PasswordResetToken.builder()
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(true)
                .build();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    void token_isValid_falseWhenExpired() {
        PasswordResetToken token = PasswordResetToken.builder()
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();
        assertThat(token.isValid()).isFalse();
    }
}
