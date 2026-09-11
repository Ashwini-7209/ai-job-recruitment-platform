package com.jobplatform.candidate;

import com.jobplatform.candidate.dto.CandidateProfileResponse;
import com.jobplatform.candidate.dto.CandidateProfileUpdateRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CandidateProfileServiceTests {

    @Autowired
    private CandidateProfileService candidateProfileService;

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedCandidate;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .fullName("Test Candidate")
                .email("candidate@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CANDIDATE)
                .enabled(true)
                .build();
        savedCandidate = userRepository.save(user);
    }

    @Test
    void createProfile_createsProfileForCandidate() {
        CandidateProfile profile = candidateProfileService.createProfile(savedCandidate);

        assertThat(profile).isNotNull();
        assertThat(profile.getId()).isNotNull();
        assertThat(profile.getUser().getId()).isEqualTo(savedCandidate.getId());
    }

    @Test
    void createProfile_throwsException_forNonCandidate() {
        User recruiter = User.builder()
                .fullName("Test Recruiter")
                .email("recruiter@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        User savedRecruiter = userRepository.save(recruiter);

        assertThatThrownBy(() -> candidateProfileService.createProfile(savedRecruiter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only CANDIDATE users");
    }

    @Test
    void getProfileByUserId_returnsProfile() {
        candidateProfileService.createProfile(savedCandidate);

        CandidateProfile profile = candidateProfileService.getProfileByUserId(savedCandidate.getId());

        assertThat(profile).isNotNull();
        assertThat(profile.getUser().getId()).isEqualTo(savedCandidate.getId());
    }

    @Test
    void getProfileByUserId_throwsException_whenNotExists() {
        assertThatThrownBy(() -> candidateProfileService.getProfileByUserId(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getProfileResponse_returnsResponse() {
        candidateProfileService.createProfile(savedCandidate);

        CandidateProfileResponse response = candidateProfileService.getProfileResponse(savedCandidate.getId());

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(savedCandidate.getId());
        assertThat(response.getFullName()).isEqualTo("Test Candidate");
        assertThat(response.getEmail()).isEqualTo("candidate@example.com");
    }

    @Test
    void updateProfile_updatesAllFields() {
        candidateProfileService.createProfile(savedCandidate);

        CandidateProfileUpdateRequest request = CandidateProfileUpdateRequest.builder()
                .phone("1234567890")
                .location("New York")
                .headline("Senior Developer")
                .bio("Experienced developer with 10 years of experience")
                .currentJobTitle("Tech Lead")
                .yearsOfExperience(10)
                .educationSummary("BS Computer Science, MIT")
                .skillsSummary("Java, Spring Boot, React")
                .linkedinUrl("https://linkedin.com/in/test")
                .githubUrl("https://github.com/test")
                .portfolioUrl("https://test.dev")
                .profileImageUrl("https://example.com/photo.jpg")
                .build();

        CandidateProfileResponse response = candidateProfileService.updateProfile(savedCandidate.getId(), request);

        assertThat(response.getPhone()).isEqualTo("1234567890");
        assertThat(response.getLocation()).isEqualTo("New York");
        assertThat(response.getHeadline()).isEqualTo("Senior Developer");
        assertThat(response.getBio()).isEqualTo("Experienced developer with 10 years of experience");
        assertThat(response.getCurrentJobTitle()).isEqualTo("Tech Lead");
        assertThat(response.getYearsOfExperience()).isEqualTo(10);
        assertThat(response.getEducationSummary()).isEqualTo("BS Computer Science, MIT");
        assertThat(response.getSkillsSummary()).isEqualTo("Java, Spring Boot, React");
        assertThat(response.getLinkedinUrl()).isEqualTo("https://linkedin.com/in/test");
        assertThat(response.getGithubUrl()).isEqualTo("https://github.com/test");
        assertThat(response.getPortfolioUrl()).isEqualTo("https://test.dev");
        assertThat(response.getProfileImageUrl()).isEqualTo("https://example.com/photo.jpg");
    }

    @Test
    void updateProfile_updatesPartialFields() {
        candidateProfileService.createProfile(savedCandidate);

        CandidateProfileUpdateRequest request = CandidateProfileUpdateRequest.builder()
                .phone("1234567890")
                .headline("Developer")
                .build();

        CandidateProfileResponse response = candidateProfileService.updateProfile(savedCandidate.getId(), request);

        assertThat(response.getPhone()).isEqualTo("1234567890");
        assertThat(response.getHeadline()).isEqualTo("Developer");
        assertThat(response.getLocation()).isNull();
        assertThat(response.getBio()).isNull();
    }

    @Test
    void updateProfile_throwsException_whenProfileNotExists() {
        CandidateProfileUpdateRequest request = CandidateProfileUpdateRequest.builder()
                .phone("1234567890")
                .build();

        assertThatThrownBy(() -> candidateProfileService.updateProfile(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProfile_updatesTimestamp() {
        CandidateProfile profile = candidateProfileService.createProfile(savedCandidate);
        CandidateProfileResponse initialResponse = candidateProfileService.getProfileResponse(savedCandidate.getId());

        CandidateProfileUpdateRequest request = CandidateProfileUpdateRequest.builder()
                .phone("1234567890")
                .build();

        CandidateProfileResponse response = candidateProfileService.updateProfile(savedCandidate.getId(), request);

        assertThat(response.getUpdatedAt()).isAfterOrEqualTo(initialResponse.getUpdatedAt());
    }
}
