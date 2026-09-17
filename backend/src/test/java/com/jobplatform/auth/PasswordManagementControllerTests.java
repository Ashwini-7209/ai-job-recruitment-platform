package com.jobplatform.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.auth.dto.ChangePasswordRequest;
import com.jobplatform.auth.dto.ForgotPasswordRequest;
import com.jobplatform.auth.dto.LoginRequest;
import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.auth.dto.ResetPasswordRequest;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRepository;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PasswordManagementControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetService passwordResetService;

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

    private String loginAndGetToken(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("data").path("accessToken").asText();
    }

    private String generateSecureToken() throws Exception {
        Method method = PasswordResetService.class.getDeclaredMethod("generateSecureToken");
        method.setAccessible(true);
        return (String) method.invoke(passwordResetService);
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
    void changePassword_validRequest_returns200() throws Exception {
        String token = loginAndGetToken(testUser.getEmail(), "password123");
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("password123")
                .newPassword("newPassword456")
                .confirmPassword("newPassword456")
                .build();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword456", updated.getPassword())).isTrue();
    }

    @Test
    void changePassword_wrongCurrentPassword_returns400() throws Exception {
        String token = loginAndGetToken(testUser.getEmail(), "password123");
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("wrongPassword")
                .newPassword("newPassword456")
                .confirmPassword("newPassword456")
                .build();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void changePassword_passwordsDoNotMatch_returns400() throws Exception {
        String token = loginAndGetToken(testUser.getEmail(), "password123");
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("password123")
                .newPassword("newPassword456")
                .confirmPassword("differentPassword")
                .build();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Passwords do not match"));
    }

    @Test
    void changePassword_tooShort_returns400() throws Exception {
        String token = loginAndGetToken(testUser.getEmail(), "password123");
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("password123")
                .newPassword("short")
                .confirmPassword("short")
                .build();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_unauthenticated_returns401() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("password123")
                .newPassword("newPassword456")
                .confirmPassword("newPassword456")
                .build();

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_missingFields_returns400() throws Exception {
        String token = loginAndGetToken(testUser.getEmail(), "password123");
        ChangePasswordRequest request = ChangePasswordRequest.builder().build();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_recruiterCanChangePassword_returns200() throws Exception {
        User recruiter = User.builder()
                .fullName("Recruiter")
                .email("recruiter-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        userRepository.save(recruiter);

        String token = loginAndGetToken(recruiter.getEmail(), "password123");
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("password123")
                .newPassword("newRecruiterPass456")
                .confirmPassword("newRecruiterPass456")
                .build();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changePassword_adminCanChangePassword_returns200() throws Exception {
        User admin = User.builder()
                .fullName("Admin")
                .email("admin-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);

        String token = loginAndGetToken(admin.getEmail(), "password123");
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("password123")
                .newPassword("newAdminPass456")
                .confirmPassword("newAdminPass456")
                .build();

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ========== FORGOT PASSWORD TESTS ==========

    @Test
    void forgotPassword_existingEmail_returns200() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email(testUser.getEmail())
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If an account exists for this email, password reset instructions have been sent."));

        assertThat(tokenRepository.findAll()).isNotEmpty();
    }

    @Test
    void forgotPassword_nonExistentEmail_returns200() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("nonexistent@example.com")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    void forgotPassword_invalidEmail_returns400() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPassword_missingEmail_returns400() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder().build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPassword_deletesPreviousTokens() throws Exception {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email(testUser.getEmail())
                .build();

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        long count = tokenRepository.findAll().stream()
                .filter(t -> t.getUser().getId().equals(testUser.getId()))
                .count();
        assertThat(count).isEqualTo(1);
    }

    // ========== RESET PASSWORD TESTS ==========

    @Test
    void resetPassword_validToken_returns200() throws Exception {
        String rawToken = generateSecureToken();
        createTokenForUser(testUser, rawToken);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token(rawToken)
                .newPassword("resetPass7890")
                .confirmPassword("resetPass7890")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successful"));

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("resetPass7890", updated.getPassword())).isTrue();
    }

    @Test
    void resetPassword_invalidToken_returns400() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("invalid-token-123")
                .newPassword("resetPass7890")
                .confirmPassword("resetPass7890")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void resetPassword_expiredToken_returns400() throws Exception {
        String rawToken = "expired-reset-token";
        String tokenHash = hashToken(rawToken);
        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .user(testUser)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();
        tokenRepository.save(expiredToken);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token(rawToken)
                .newPassword("resetPass7890")
                .confirmPassword("resetPass7890")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Reset token has expired"));
    }

    @Test
    void resetPassword_usedToken_returns400() throws Exception {
        String rawToken = "used-reset-token";
        String tokenHash = hashToken(rawToken);
        PasswordResetToken usedToken = PasswordResetToken.builder()
                .user(testUser)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(true)
                .usedAt(LocalDateTime.now().minusMinutes(5))
                .build();
        tokenRepository.save(usedToken);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token(rawToken)
                .newPassword("resetPass7890")
                .confirmPassword("resetPass7890")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Reset token has already been used"));
    }

    @Test
    void resetPassword_passwordsDoNotMatch_returns400() throws Exception {
        String rawToken = "mismatch-reset-token";
        createTokenForUser(testUser, rawToken);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token(rawToken)
                .newPassword("resetPass7890")
                .confirmPassword("differentPass7890")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Passwords do not match"));
    }

    @Test
    void resetPassword_missingFields_returns400() throws Exception {
        ResetPasswordRequest request = ResetPasswordRequest.builder().build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_tooShortPassword_returns400() throws Exception {
        String rawToken = "short-pw-reset-token";
        createTokenForUser(testUser, rawToken);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token(rawToken)
                .newPassword("short")
                .confirmPassword("short")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_tokenMarkedAsUsed() throws Exception {
        String rawToken = "mark-used-token";
        PasswordResetToken savedToken = createTokenForUser(testUser, rawToken);

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token(rawToken)
                .newPassword("resetPass7890")
                .confirmPassword("resetPass7890")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        PasswordResetToken updatedToken = tokenRepository.findById(savedToken.getId()).orElseThrow();
        assertThat(updatedToken.getUsed()).isTrue();
        assertThat(updatedToken.getUsedAt()).isNotNull();
    }

    @Test
    void resetPassword_canLoginWithNewPassword() throws Exception {
        String rawToken = generateSecureToken();
        createTokenForUser(testUser, rawToken);

        ResetPasswordRequest resetRequest = ResetPasswordRequest.builder()
                .token(rawToken)
                .newPassword("newLoginPass123")
                .confirmPassword("newLoginPass123")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest(testUser.getEmail(), "newLoginPass123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void resetPassword_oldPasswordNoLongerWorks() throws Exception {
        String rawToken = generateSecureToken();
        createTokenForUser(testUser, rawToken);

        ResetPasswordRequest resetRequest = ResetPasswordRequest.builder()
                .token(rawToken)
                .newPassword("newLoginPass123")
                .confirmPassword("newLoginPass123")
                .build();

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk());

        LoginRequest loginRequest = new LoginRequest(testUser.getEmail(), "password123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
