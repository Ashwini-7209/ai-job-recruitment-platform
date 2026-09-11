package com.jobplatform.recruiter;

import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.recruiter.dto.RecruiterProfileResponse;
import com.jobplatform.recruiter.dto.RecruiterProfileUpdateRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RecruiterProfileServiceTests {

    @Autowired
    private RecruiterProfileService recruiterProfileService;

    @Autowired
    private RecruiterProfileRepository recruiterProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedRecruiter;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .fullName("Test Recruiter")
                .email("recruiter@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        savedRecruiter = userRepository.save(user);
    }

    @Test
    void createProfile_createsProfileForRecruiter() {
        RecruiterProfile profile = recruiterProfileService.createProfile(savedRecruiter);

        assertThat(profile).isNotNull();
        assertThat(profile.getId()).isNotNull();
        assertThat(profile.getUser().getId()).isEqualTo(savedRecruiter.getId());
    }

    @Test
    void createProfile_throwsException_forNonRecruiter() {
        User candidate = User.builder()
                .fullName("Test Candidate")
                .email("candidate@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CANDIDATE)
                .enabled(true)
                .build();
        User savedCandidate = userRepository.save(candidate);

        assertThatThrownBy(() -> recruiterProfileService.createProfile(savedCandidate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only RECRUITER users");
    }

    @Test
    void getProfileByUserId_returnsProfile() {
        recruiterProfileService.createProfile(savedRecruiter);

        RecruiterProfile profile = recruiterProfileService.getProfileByUserId(savedRecruiter.getId());

        assertThat(profile).isNotNull();
        assertThat(profile.getUser().getId()).isEqualTo(savedRecruiter.getId());
    }

    @Test
    void getProfileByUserId_throwsException_whenNotExists() {
        assertThatThrownBy(() -> recruiterProfileService.getProfileByUserId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProfileResponse_returnsResponse() {
        recruiterProfileService.createProfile(savedRecruiter);

        RecruiterProfileResponse response = recruiterProfileService.getProfileResponse(savedRecruiter.getId());

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(savedRecruiter.getId());
        assertThat(response.getFullName()).isEqualTo("Test Recruiter");
        assertThat(response.getEmail()).isEqualTo("recruiter@example.com");
    }

    @Test
    void updateProfile_updatesAllFields() {
        recruiterProfileService.createProfile(savedRecruiter);

        RecruiterProfileUpdateRequest request = RecruiterProfileUpdateRequest.builder()
                .phone("0987654321")
                .jobTitle("Senior Recruiter")
                .department("Engineering")
                .companyName("Tech Corp")
                .companyWebsite("https://techcorp.com")
                .companyDescription("Leading tech company")
                .companyLocation("San Francisco")
                .linkedinUrl("https://linkedin.com/in/recruiter")
                .build();

        RecruiterProfileResponse response = recruiterProfileService.updateProfile(savedRecruiter.getId(), request);

        assertThat(response.getPhone()).isEqualTo("0987654321");
        assertThat(response.getJobTitle()).isEqualTo("Senior Recruiter");
        assertThat(response.getDepartment()).isEqualTo("Engineering");
        assertThat(response.getCompanyName()).isEqualTo("Tech Corp");
        assertThat(response.getCompanyWebsite()).isEqualTo("https://techcorp.com");
        assertThat(response.getCompanyDescription()).isEqualTo("Leading tech company");
        assertThat(response.getCompanyLocation()).isEqualTo("San Francisco");
        assertThat(response.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/recruiter");
    }

    @Test
    void updateProfile_updatesPartialFields() {
        recruiterProfileService.createProfile(savedRecruiter);

        RecruiterProfileUpdateRequest request = RecruiterProfileUpdateRequest.builder()
                .phone("0987654321")
                .companyName("Tech Corp")
                .build();

        RecruiterProfileResponse response = recruiterProfileService.updateProfile(savedRecruiter.getId(), request);

        assertThat(response.getPhone()).isEqualTo("0987654321");
        assertThat(response.getCompanyName()).isEqualTo("Tech Corp");
        assertThat(response.getJobTitle()).isNull();
        assertThat(response.getDepartment()).isNull();
    }

    @Test
    void updateProfile_throwsException_whenProfileNotExists() {
        RecruiterProfileUpdateRequest request = RecruiterProfileUpdateRequest.builder()
                .phone("0987654321")
                .build();

        assertThatThrownBy(() -> recruiterProfileService.updateProfile(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProfile_updatesTimestamp() {
        RecruiterProfile profile = recruiterProfileService.createProfile(savedRecruiter);
        RecruiterProfileResponse initialResponse = recruiterProfileService.getProfileResponse(savedRecruiter.getId());

        RecruiterProfileUpdateRequest request = RecruiterProfileUpdateRequest.builder()
                .phone("0987654321")
                .build();

        RecruiterProfileResponse response = recruiterProfileService.updateProfile(savedRecruiter.getId(), request);

        assertThat(response.getUpdatedAt()).isAfterOrEqualTo(initialResponse.getUpdatedAt());
    }
}
