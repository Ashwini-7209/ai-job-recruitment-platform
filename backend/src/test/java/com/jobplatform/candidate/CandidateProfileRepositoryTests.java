package com.jobplatform.candidate;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CandidateProfileRepositoryTests {

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .fullName("Test Candidate")
                .email("candidate@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CANDIDATE)
                .enabled(true)
                .build();
        savedUser = userRepository.save(user);
    }

    @Test
    void save_createsProfile() {
        CandidateProfile profile = CandidateProfile.builder()
                .user(savedUser)
                .phone("1234567890")
                .location("New York")
                .build();

        CandidateProfile saved = candidateProfileRepository.save(profile);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(saved.getPhone()).isEqualTo("1234567890");
        assertThat(saved.getLocation()).isEqualTo("New York");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByUser_returnsProfile() {
        CandidateProfile profile = CandidateProfile.builder()
                .user(savedUser)
                .build();
        candidateProfileRepository.save(profile);

        Optional<CandidateProfile> found = candidateProfileRepository.findByUser(savedUser);

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void findByUser_returnsEmpty_whenNoProfile() {
        Optional<CandidateProfile> found = candidateProfileRepository.findByUser(savedUser);
        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_returnsProfile() {
        CandidateProfile profile = CandidateProfile.builder()
                .user(savedUser)
                .build();
        candidateProfileRepository.save(profile);

        Optional<CandidateProfile> found = candidateProfileRepository.findByUserId(savedUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void findByUserId_returnsEmpty_whenNoProfile() {
        Optional<CandidateProfile> found = candidateProfileRepository.findByUserId(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUserId_returnsTrue_whenExists() {
        CandidateProfile profile = CandidateProfile.builder()
                .user(savedUser)
                .build();
        candidateProfileRepository.save(profile);

        boolean exists = candidateProfileRepository.existsByUserId(savedUser.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserId_returnsFalse_whenNotExists() {
        boolean exists = candidateProfileRepository.existsByUserId(999L);
        assertThat(exists).isFalse();
    }

    @Test
    void save_profileWithOptionalFields() {
        CandidateProfile profile = CandidateProfile.builder()
                .user(savedUser)
                .headline("Software Engineer")
                .bio("Experienced developer")
                .yearsOfExperience(5)
                .linkedinUrl("https://linkedin.com/in/test")
                .githubUrl("https://github.com/test")
                .build();

        CandidateProfile saved = candidateProfileRepository.save(profile);

        assertThat(saved.getHeadline()).isEqualTo("Software Engineer");
        assertThat(saved.getBio()).isEqualTo("Experienced developer");
        assertThat(saved.getYearsOfExperience()).isEqualTo(5);
        assertThat(saved.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/test");
        assertThat(saved.getGithubUrl()).isEqualTo("https://github.com/test");
    }

    @Test
    void count_increasesAfterSave() {
        long initialCount = candidateProfileRepository.count();

        CandidateProfile profile = CandidateProfile.builder()
                .user(savedUser)
                .build();
        candidateProfileRepository.save(profile);

        assertThat(candidateProfileRepository.count()).isEqualTo(initialCount + 1);
    }
}
