package com.jobplatform.auth;

import com.jobplatform.auth.dto.RegisterRequest;
import com.jobplatform.auth.dto.RegisterResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.user.UserRepository;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private RegisterRequest createValidCandidateRequest() {
        return RegisterRequest.builder()
                .fullName("John Doe")
                .email("john@example.com")
                .password("password123")
                .confirmPassword("password123")
                .role(UserRole.CANDIDATE)
                .build();
    }

    private RegisterRequest createValidRecruiterRequest() {
        return RegisterRequest.builder()
                .fullName("Jane Smith")
                .email("jane@example.com")
                .password("securePass456")
                .confirmPassword("securePass456")
                .role(UserRole.RECRUITER)
                .build();
    }

    @Test
    void registerCandidate_success() {
        RegisterRequest request = createValidCandidateRequest();

        RegisterResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getFullName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.CANDIDATE);
        assertThat(response.getEnabled()).isTrue();
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void registerRecruiter_success() {
        RegisterRequest request = createValidRecruiterRequest();

        RegisterResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getFullName()).isEqualTo("Jane Smith");
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getRole()).isEqualTo(UserRole.RECRUITER);
        assertThat(response.getEnabled()).isTrue();
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request1 = createValidCandidateRequest();
        authService.register(request1);

        RegisterRequest request2 = RegisterRequest.builder()
                .fullName("Another User")
                .email("john@example.com")
                .password("differentPass123")
                .confirmPassword("differentPass123")
                .role(UserRole.RECRUITER)
                .build();

        assertThatThrownBy(() -> authService.register(request2))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void register_passwordMismatch_throwsException() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("mismatch@example.com")
                .password("password123")
                .confirmPassword("differentPassword")
                .role(UserRole.CANDIDATE)
                .build();

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    void register_invalidEmail_throwsException() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("not-an-email")
                .password("password123")
                .confirmPassword("password123")
                .role(UserRole.CANDIDATE)
                .build();

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void register_adminRole_throwsException() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Admin User")
                .email("admin@example.com")
                .password("adminPass123")
                .confirmPassword("adminPass123")
                .role(UserRole.ADMIN)
                .build();

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Admin registration is not allowed");
    }

    @Test
    void register_passwordIsHashed_beforePersistence() {
        RegisterRequest request = createValidCandidateRequest();

        RegisterResponse response = authService.register(request);

        String storedPassword = userRepository.findById(response.getId())
                .orElseThrow()
                .getPassword();

        assertThat(storedPassword).isNotEqualTo("password123");
        assertThat(storedPassword).startsWith("$2a$");
        assertThat(passwordEncoder.matches("password123", storedPassword)).isTrue();
    }

    @Test
    void register_responseDoesNotContainPassword() {
        RegisterRequest request = createValidCandidateRequest();

        RegisterResponse response = authService.register(request);

        assertThat(response.toString()).doesNotContain("password");
        assertThat(response.toString()).doesNotContain("password123");
    }

    @Test
    void register_emailNormalized_toLowerCase() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("  JOHN@EXAMPLE.COM  ")
                .password("password123")
                .confirmPassword("password123")
                .role(UserRole.CANDIDATE)
                .build();

        RegisterResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void register_duplicateEmail_caseInsensitive_throwsException() {
        RegisterRequest request1 = createValidCandidateRequest();
        authService.register(request1);

        RegisterRequest request2 = RegisterRequest.builder()
                .fullName("Another User")
                .email("JOHN@EXAMPLE.COM")
                .password("differentPass123")
                .confirmPassword("differentPass123")
                .role(UserRole.RECRUITER)
                .build();

        assertThatThrownBy(() -> authService.register(request2))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }
}
