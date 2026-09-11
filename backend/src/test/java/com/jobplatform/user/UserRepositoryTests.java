package com.jobplatform.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    private User createTestUser(String email, UserRole role) {
        return User.builder()
                .fullName("Test User")
                .email(email)
                .password("hashed_password_123")
                .role(role)
                .enabled(true)
                .build();
    }

    @Test
    void saveUser_success() {
        User user = createTestUser("test@example.com", UserRole.CANDIDATE);
        User saved = userRepository.save(user);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFullName()).isEqualTo("Test User");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
        assertThat(saved.getRole()).isEqualTo(UserRole.CANDIDATE);
        assertThat(saved.getEnabled()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByEmail_found() {
        User user = createTestUser("findme@example.com", UserRole.RECRUITER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("findme@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("findme@example.com");
        assertThat(found.get().getRole()).isEqualTo(UserRole.RECRUITER);
    }

    @Test
    void findByEmail_notFound() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_true() {
        User user = createTestUser("exist@example.com", UserRole.ADMIN);
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("exist@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_false() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void saveUser_duplicateEmail_throwsException() {
        User user1 = createTestUser("duplicate@example.com", UserRole.CANDIDATE);
        userRepository.save(user1);
        userRepository.flush();

        User user2 = createTestUser("duplicate@example.com", UserRole.RECRUITER);

        assertThatThrownBy(() -> userRepository.save(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void saveUser_roleCandidate_persistsCorrectly() {
        User user = createTestUser("candidate@example.com", UserRole.CANDIDATE);
        User saved = userRepository.save(user);

        assertThat(saved.getRole()).isEqualTo(UserRole.CANDIDATE);
    }

    @Test
    void saveUser_roleRecruiter_persistsCorrectly() {
        User user = createTestUser("recruiter@example.com", UserRole.RECRUITER);
        User saved = userRepository.save(user);

        assertThat(saved.getRole()).isEqualTo(UserRole.RECRUITER);
    }

    @Test
    void saveUser_roleAdmin_persistsCorrectly() {
        User user = createTestUser("admin@example.com", UserRole.ADMIN);
        User saved = userRepository.save(user);

        assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void saveUser_timestampsPopulated() {
        User user = createTestUser("timestamps@example.com", UserRole.CANDIDATE);
        User saved = userRepository.save(user);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
    }
}
