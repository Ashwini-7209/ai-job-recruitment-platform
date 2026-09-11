package com.jobplatform.job;

import com.jobplatform.common.PagedResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.job.dto.CreateJobRequest;
import com.jobplatform.job.dto.JobResponse;
import com.jobplatform.job.dto.JobSummaryResponse;
import com.jobplatform.job.dto.RecruiterJobStats;
import com.jobplatform.job.dto.UpdateJobRequest;
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
class JobServiceTests {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedRecruiter;
    private User savedCandidate;

    @BeforeEach
    void setUp() {
        User recruiter = User.builder()
                .fullName("Test Recruiter")
                .email("recruiter-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        savedRecruiter = userRepository.save(recruiter);

        User candidate = User.builder()
                .fullName("Test Candidate")
                .email("candidate-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CANDIDATE)
                .enabled(true)
                .build();
        savedCandidate = userRepository.save(candidate);
    }

    private CreateJobRequest createValidJobRequest() {
        return CreateJobRequest.builder()
                .title("Software Engineer")
                .description("We are looking for a software engineer")
                .location("New York")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.HYBRID)
                .experienceMin(2)
                .experienceMax(5)
                .salaryMin(80000)
                .salaryMax(120000)
                .skills("Java, Spring Boot, React")
                .build();
    }

    @Test
    void createJob_success() {
        CreateJobRequest request = createValidJobRequest();

        JobResponse response = jobService.createJob(savedRecruiter, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Software Engineer");
        assertThat(response.getRecruiterId()).isEqualTo(savedRecruiter.getId());
        assertThat(response.getStatus()).isEqualTo(JobStatus.DRAFT);
    }

    @Test
    void createJob_throwsException_forNonRecruiter() {
        CreateJobRequest request = createValidJobRequest();

        assertThatThrownBy(() -> jobService.createJob(savedCandidate, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only recruiters");
    }

    @Test
    void createJob_validatesExperienceRange() {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("Job")
                .description("Description")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.REMOTE)
                .experienceMin(5)
                .experienceMax(2)
                .build();

        assertThatThrownBy(() -> jobService.createJob(savedRecruiter, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createJob_validatesSalaryRange() {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("Job")
                .description("Description")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.REMOTE)
                .salaryMin(100000)
                .salaryMax(50000)
                .build();

        assertThatThrownBy(() -> jobService.createJob(savedRecruiter, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createJob_validatesPastDeadline() {
        CreateJobRequest request = CreateJobRequest.builder()
                .title("Job")
                .description("Description")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.REMOTE)
                .applicationDeadline(LocalDateTime.now().minusDays(1))
                .build();

        assertThatThrownBy(() -> jobService.createJob(savedRecruiter, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("future");
    }

    @Test
    void getRecruiterJobs_returnsJobs() {
        jobService.createJob(savedRecruiter, createValidJobRequest());

        PagedResponse<JobSummaryResponse> response = jobService.getRecruiterJobs(savedRecruiter, 0, 10, null);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getRecruiterJobs_filtersByStatus() {
        jobService.createJob(savedRecruiter, createValidJobRequest());
        jobService.createJob(savedRecruiter, createValidJobRequest());

        PagedResponse<JobSummaryResponse> draftJobs = jobService.getRecruiterJobs(savedRecruiter, 0, 10, JobStatus.DRAFT);

        assertThat(draftJobs.getContent()).hasSize(2);
    }

    @Test
    void getRecruiterJobById_returnsJob() {
        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());

        JobResponse response = jobService.getRecruiterJobById(savedRecruiter, created.getId());

        assertThat(response.getId()).isEqualTo(created.getId());
        assertThat(response.getTitle()).isEqualTo("Software Engineer");
    }

    @Test
    void getRecruiterJobById_throwsException_whenNotOwner() {
        User otherRecruiter = User.builder()
                .fullName("Other Recruiter")
                .email("other-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        User savedOther = userRepository.save(otherRecruiter);

        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());

        assertThatThrownBy(() -> jobService.getRecruiterJobById(savedOther, created.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void updateJob_updatesFields() {
        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());

        UpdateJobRequest updateRequest = UpdateJobRequest.builder()
                .title("Updated Title")
                .location("San Francisco")
                .build();

        JobResponse response = jobService.updateJob(savedRecruiter, created.getId(), updateRequest);

        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getLocation()).isEqualTo("San Francisco");
    }

    @Test
    void updateJob_throwsException_whenNotOwner() {
        User otherRecruiter = User.builder()
                .fullName("Other Recruiter")
                .email("other-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        User savedOther = userRepository.save(otherRecruiter);

        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());

        UpdateJobRequest updateRequest = UpdateJobRequest.builder().title("Hacked").build();

        assertThatThrownBy(() -> jobService.updateJob(savedOther, created.getId(), updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void publishJob_success() {
        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());

        JobResponse response = jobService.publishJob(savedRecruiter, created.getId());

        assertThat(response.getStatus()).isEqualTo(JobStatus.PUBLISHED);
        assertThat(response.getPublishedAt()).isNotNull();
    }

    @Test
    void publishJob_throwsException_whenNotDraft() {
        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());
        jobService.publishJob(savedRecruiter, created.getId());

        assertThatThrownBy(() -> jobService.publishJob(savedRecruiter, created.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("draft");
    }

    @Test
    void publishJob_throwsException_whenNotOwner() {
        User otherRecruiter = User.builder()
                .fullName("Other Recruiter")
                .email("other-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        User savedOther = userRepository.save(otherRecruiter);

        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());

        assertThatThrownBy(() -> jobService.publishJob(savedOther, created.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void closeJob_success() {
        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());
        jobService.publishJob(savedRecruiter, created.getId());

        JobResponse response = jobService.closeJob(savedRecruiter, created.getId());

        assertThat(response.getStatus()).isEqualTo(JobStatus.CLOSED);
    }

    @Test
    void closeJob_throwsException_whenNotPublished() {
        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());

        assertThatThrownBy(() -> jobService.closeJob(savedRecruiter, created.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("published");
    }

    @Test
    void closeJob_throwsException_whenNotOwner() {
        User otherRecruiter = User.builder()
                .fullName("Other Recruiter")
                .email("other-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        User savedOther = userRepository.save(otherRecruiter);

        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());
        jobService.publishJob(savedRecruiter, created.getId());

        assertThatThrownBy(() -> jobService.closeJob(savedOther, created.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void deleteJob_success() {
        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());

        jobService.deleteJob(savedRecruiter, created.getId());

        assertThatThrownBy(() -> jobService.getRecruiterJobById(savedRecruiter, created.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteJob_throwsException_whenPublished() {
        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());
        jobService.publishJob(savedRecruiter, created.getId());

        assertThatThrownBy(() -> jobService.deleteJob(savedRecruiter, created.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("published");
    }

    @Test
    void deleteJob_throwsException_whenNotOwner() {
        User otherRecruiter = User.builder()
                .fullName("Other Recruiter")
                .email("other-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        User savedOther = userRepository.save(otherRecruiter);

        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());

        assertThatThrownBy(() -> jobService.deleteJob(savedOther, created.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("permission");
    }

    @Test
    void getRecruiterStats_returnsCorrectCounts() {
        jobService.createJob(savedRecruiter, createValidJobRequest());
        JobResponse published = jobService.createJob(savedRecruiter, createValidJobRequest());
        jobService.publishJob(savedRecruiter, published.getId());

        RecruiterJobStats stats = jobService.getRecruiterStats(savedRecruiter);

        assertThat(stats.getTotalJobs()).isEqualTo(2);
        assertThat(stats.getDraftJobs()).isEqualTo(1);
        assertThat(stats.getPublishedJobs()).isEqualTo(1);
        assertThat(stats.getClosedJobs()).isEqualTo(0);
    }

    @Test
    void getPublishedJobs_returnsOnlyPublished() {
        jobService.createJob(savedRecruiter, createValidJobRequest());
        JobResponse published = jobService.createJob(savedRecruiter, createValidJobRequest());
        jobService.publishJob(savedRecruiter, published.getId());

        PagedResponse<JobSummaryResponse> response = jobService.getPublishedJobs(0, 10, "newest");

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void getPublishedJobById_returnsJob() {
        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());
        jobService.publishJob(savedRecruiter, created.getId());

        JobResponse response = jobService.getPublishedJobById(created.getId());

        assertThat(response.getId()).isEqualTo(created.getId());
    }

    @Test
    void getPublishedJobById_throwsException_whenDraft() {
        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());

        assertThatThrownBy(() -> jobService.getPublishedJobById(created.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPublishedJobById_throwsException_whenClosed() {
        JobResponse created = jobService.createJob(savedRecruiter, createValidJobRequest());
        jobService.publishJob(savedRecruiter, created.getId());
        jobService.closeJob(savedRecruiter, created.getId());

        assertThatThrownBy(() -> jobService.getPublishedJobById(created.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
