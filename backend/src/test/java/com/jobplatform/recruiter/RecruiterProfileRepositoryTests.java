package com.jobplatform.recruiter;

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
class RecruiterProfileRepositoryTests {

    @Autowired
    private RecruiterProfileRepository recruiterProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedUser;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .fullName("Test Recruiter")
                .email("recruiter@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        savedUser = userRepository.save(user);
    }

    @Test
    void save_createsProfile() {
        RecruiterProfile profile = RecruiterProfile.builder()
                .user(savedUser)
                .companyName("Tech Corp")
                .jobTitle("HR Manager")
                .build();

        RecruiterProfile saved = recruiterProfileRepository.save(profile);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(saved.getCompanyName()).isEqualTo("Tech Corp");
        assertThat(saved.getJobTitle()).isEqualTo("HR Manager");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByUser_returnsProfile() {
        RecruiterProfile profile = RecruiterProfile.builder()
                .user(savedUser)
                .build();
        recruiterProfileRepository.save(profile);

        Optional<RecruiterProfile> found = recruiterProfileRepository.findByUser(savedUser);

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void findByUser_returnsEmpty_whenNoProfile() {
        Optional<RecruiterProfile> found = recruiterProfileRepository.findByUser(savedUser);
        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_returnsProfile() {
        RecruiterProfile profile = RecruiterProfile.builder()
                .user(savedUser)
                .build();
        recruiterProfileRepository.save(profile);

        Optional<RecruiterProfile> found = recruiterProfileRepository.findByUserId(savedUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void findByUserId_returnsEmpty_whenNoProfile() {
        Optional<RecruiterProfile> found = recruiterProfileRepository.findByUserId(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUserId_returnsTrue_whenExists() {
        RecruiterProfile profile = RecruiterProfile.builder()
                .user(savedUser)
                .build();
        recruiterProfileRepository.save(profile);

        boolean exists = recruiterProfileRepository.existsByUserId(savedUser.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserId_returnsFalse_whenNotExists() {
        boolean exists = recruiterProfileRepository.existsByUserId(999L);
        assertThat(exists).isFalse();
    }

    @Test
    void save_profileWithOptionalFields() {
        RecruiterProfile profile = RecruiterProfile.builder()
                .user(savedUser)
                .phone("0987654321")
                .jobTitle("Senior Recruiter")
                .department("Engineering")
                .companyName("Tech Corp")
                .companyWebsite("https://techcorp.com")
                .companyDescription("Leading tech company")
                .companyLocation("San Francisco")
                .linkedinUrl("https://linkedin.com/in/recruiter")
                .build();

        RecruiterProfile saved = recruiterProfileRepository.save(profile);

        assertThat(saved.getPhone()).isEqualTo("0987654321");
        assertThat(saved.getJobTitle()).isEqualTo("Senior Recruiter");
        assertThat(saved.getDepartment()).isEqualTo("Engineering");
        assertThat(saved.getCompanyName()).isEqualTo("Tech Corp");
        assertThat(saved.getCompanyWebsite()).isEqualTo("https://techcorp.com");
        assertThat(saved.getCompanyDescription()).isEqualTo("Leading tech company");
        assertThat(saved.getCompanyLocation()).isEqualTo("San Francisco");
        assertThat(saved.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/recruiter");
    }

    @Test
    void count_increasesAfterSave() {
        long initialCount = recruiterProfileRepository.count();

        RecruiterProfile profile = RecruiterProfile.builder()
                .user(savedUser)
                .build();
        recruiterProfileRepository.save(profile);

        assertThat(recruiterProfileRepository.count()).isEqualTo(initialCount + 1);
    }
}
