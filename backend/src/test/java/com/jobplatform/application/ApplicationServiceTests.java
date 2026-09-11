package com.jobplatform.application;

import com.jobplatform.application.dto.ApplicationResponse;
import com.jobplatform.application.dto.ApplicationStats;
import com.jobplatform.application.dto.ApplicationSummaryResponse;
import com.jobplatform.application.dto.CreateApplicationRequest;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.job.enums.WorkplaceType;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApplicationServiceTests {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedCandidate;
    private User savedRecruiter;
    private User savedOtherRecruiter;
    private Job savedJob;

    @BeforeEach
    void setUp() {
        User candidate = User.builder()
                .fullName("Test Candidate")
                .email("candidate-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CANDIDATE)
                .enabled(true)
                .build();
        savedCandidate = userRepository.save(candidate);

        User recruiter = User.builder()
                .fullName("Test Recruiter")
                .email("recruiter-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        savedRecruiter = userRepository.save(recruiter);

        User otherRecruiter = User.builder()
                .fullName("Other Recruiter")
                .email("other-recruiter-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        savedOtherRecruiter = userRepository.save(otherRecruiter);

        Job job = Job.builder()
                .recruiter(savedRecruiter)
                .title("Software Engineer")
                .description("We are looking for a software engineer")
                .location("New York")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.HYBRID)
                .status(JobStatus.PUBLISHED)
                .build();
        savedJob = jobRepository.save(job);
    }

    @Test
    void applyToJob_success() {
        CreateApplicationRequest request = CreateApplicationRequest.builder()
                .coverLetter("I am interested in this position")
                .build();

        ApplicationResponse response = applicationService.applyToJob(savedCandidate, savedJob.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(response.getCandidateName()).isEqualTo("Test Candidate");
        assertThat(response.getJobTitle()).isEqualTo("Software Engineer");
    }

    @Test
    void applyToJob_success_withoutCoverLetter() {
        CreateApplicationRequest request = CreateApplicationRequest.builder().build();

        ApplicationResponse response = applicationService.applyToJob(savedCandidate, savedJob.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
    }

    @Test
    void applyToJob_throwsException_forNonCandidate() {
        CreateApplicationRequest request = CreateApplicationRequest.builder().build();

        assertThatThrownBy(() -> applicationService.applyToJob(savedRecruiter, savedJob.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only candidates");
    }

    @Test
    void applyToJob_throwsException_forDraftJob() {
        Job draftJob = Job.builder()
                .recruiter(savedRecruiter)
                .title("Draft Job")
                .description("Description")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.REMOTE)
                .status(JobStatus.DRAFT)
                .build();
        Job savedDraft = jobRepository.save(draftJob);

        CreateApplicationRequest request = CreateApplicationRequest.builder().build();

        assertThatThrownBy(() -> applicationService.applyToJob(savedCandidate, savedDraft.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("published");
    }

    @Test
    void applyToJob_throwsException_forClosedJob() {
        Job closedJob = Job.builder()
                .recruiter(savedRecruiter)
                .title("Closed Job")
                .description("Description")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.REMOTE)
                .status(JobStatus.CLOSED)
                .build();
        Job savedClosed = jobRepository.save(closedJob);

        CreateApplicationRequest request = CreateApplicationRequest.builder().build();

        assertThatThrownBy(() -> applicationService.applyToJob(savedCandidate, savedClosed.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("published");
    }

    @Test
    void applyToJob_throwsException_forDuplicateApplication() {
        CreateApplicationRequest request = CreateApplicationRequest.builder().build();
        applicationService.applyToJob(savedCandidate, savedJob.getId(), request);

        assertThatThrownBy(() -> applicationService.applyToJob(savedCandidate, savedJob.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already applied");
    }

    @Test
    void applyToJob_throwsException_forPastDeadline() {
        Job deadlineJob = Job.builder()
                .recruiter(savedRecruiter)
                .title("Deadline Job")
                .description("Description")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.REMOTE)
                .status(JobStatus.PUBLISHED)
                .applicationDeadline(LocalDateTime.now().minusDays(1))
                .build();
        Job savedDeadline = jobRepository.save(deadlineJob);

        CreateApplicationRequest request = CreateApplicationRequest.builder().build();

        assertThatThrownBy(() -> applicationService.applyToJob(savedCandidate, savedDeadline.getId(), request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("deadline");
    }

    @Test
    void applyToJob_throwsException_forNonExistentJob() {
        CreateApplicationRequest request = CreateApplicationRequest.builder().build();

        assertThatThrownBy(() -> applicationService.applyToJob(savedCandidate, 999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCandidateApplications_returnsApplications() {
        applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        PagedResponse<ApplicationSummaryResponse> response = applicationService.getCandidateApplications(savedCandidate, 0, 10, "newest");

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getCandidateApplications_throwsException_forNonCandidate() {
        assertThatThrownBy(() -> applicationService.getCandidateApplications(savedRecruiter, 0, 10, "newest"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only candidates");
    }

    @Test
    void getCandidateApplicationById_returnsApplication() {
        ApplicationResponse created = applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        ApplicationResponse response = applicationService.getCandidateApplicationById(savedCandidate, created.getId());

        assertThat(response.getId()).isEqualTo(created.getId());
    }

    @Test
    void getCandidateApplicationById_throwsException_whenNotOwner() {
        ApplicationResponse created = applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        assertThatThrownBy(() -> applicationService.getCandidateApplicationById(savedOtherRecruiter, created.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void withdrawApplication_success() {
        ApplicationResponse created = applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        ApplicationResponse response = applicationService.withdrawApplication(savedCandidate, created.getId());

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
        assertThat(response.getWithdrawnAt()).isNotNull();
    }

    @Test
    void withdrawApplication_throwsException_whenNotOwner() {
        ApplicationResponse created = applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        assertThatThrownBy(() -> applicationService.withdrawApplication(savedOtherRecruiter, created.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void withdrawApplication_success_whenUnderReview() {
        ApplicationResponse created = applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());
        applicationService.updateApplicationStatus(savedRecruiter, created.getId(), ApplicationStatus.UNDER_REVIEW);

        ApplicationResponse response = applicationService.withdrawApplication(savedCandidate, created.getId());

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    @Test
    void getRecruiterApplications_returnsApplications() {
        applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        PagedResponse<ApplicationSummaryResponse> response = applicationService.getRecruiterApplications(savedRecruiter, 0, 10, "newest");

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void getRecruiterJobApplications_returnsApplications() {
        applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        PagedResponse<ApplicationSummaryResponse> response = applicationService.getRecruiterJobApplications(savedRecruiter, savedJob.getId(), 0, 10, "newest");

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void getRecruiterJobApplications_throwsException_whenNotOwner() {
        applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        assertThatThrownBy(() -> applicationService.getRecruiterJobApplications(savedOtherRecruiter, savedJob.getId(), 0, 10, "newest"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void getRecruiterApplicationById_returnsApplication() {
        ApplicationResponse created = applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        ApplicationResponse response = applicationService.getRecruiterApplicationById(savedRecruiter, created.getId());

        assertThat(response.getId()).isEqualTo(created.getId());
    }

    @Test
    void getRecruiterApplicationById_throwsException_whenNotOwner() {
        ApplicationResponse created = applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        assertThatThrownBy(() -> applicationService.getRecruiterApplicationById(savedOtherRecruiter, created.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void updateApplicationStatus_success() {
        ApplicationResponse created = applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        ApplicationResponse response = applicationService.updateApplicationStatus(savedRecruiter, created.getId(), ApplicationStatus.UNDER_REVIEW);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
    }

    @Test
    void updateApplicationStatus_validTransitions() {
        ApplicationResponse created = applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        applicationService.updateApplicationStatus(savedRecruiter, created.getId(), ApplicationStatus.UNDER_REVIEW);
        applicationService.updateApplicationStatus(savedRecruiter, created.getId(), ApplicationStatus.SHORTLISTED);
        ApplicationResponse response = applicationService.updateApplicationStatus(savedRecruiter, created.getId(), ApplicationStatus.HIRED);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.HIRED);
    }

    @Test
    void updateApplicationStatus_throwsException_forInvalidTransition() {
        ApplicationResponse created = applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(savedRecruiter, created.getId(), ApplicationStatus.HIRED))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateApplicationStatus_throwsException_whenNotOwner() {
        ApplicationResponse created = applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        assertThatThrownBy(() -> applicationService.updateApplicationStatus(savedOtherRecruiter, created.getId(), ApplicationStatus.UNDER_REVIEW))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void getRecruiterJobStats_returnsCorrectCounts() {
        applicationService.applyToJob(savedCandidate, savedJob.getId(), CreateApplicationRequest.builder().build());

        ApplicationStats stats = applicationService.getRecruiterJobStats(savedRecruiter, savedJob.getId());

        assertThat(stats.getTotalApplications()).isEqualTo(1);
        assertThat(stats.getAppliedCount()).isEqualTo(1);
    }

    @Test
    void getRecruiterJobStats_throwsException_whenNotOwner() {
        assertThatThrownBy(() -> applicationService.getRecruiterJobStats(savedOtherRecruiter, savedJob.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("permission");
    }
}
