package com.jobplatform.application;

import com.jobplatform.application.enums.ApplicationStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApplicationRepositoryTests {

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

    private Application createApplication(User candidate, Job job, ApplicationStatus status) {
        Application application = Application.builder()
                .candidate(candidate)
                .job(job)
                .status(status)
                .build();
        return applicationRepository.save(application);
    }

    @Test
    void save_createsApplication() {
        Application application = Application.builder()
                .candidate(savedCandidate)
                .job(savedJob)
                .status(ApplicationStatus.APPLIED)
                .coverLetter("I am interested in this position")
                .build();

        Application saved = applicationRepository.save(application);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCandidate().getId()).isEqualTo(savedCandidate.getId());
        assertThat(saved.getJob().getId()).isEqualTo(savedJob.getId());
        assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(saved.getAppliedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByCandidateAndJob_returnsApplication() {
        Application application = createApplication(savedCandidate, savedJob, ApplicationStatus.APPLIED);

        Optional<Application> found = applicationRepository.findByCandidateAndJob(savedCandidate, savedJob);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(application.getId());
    }

    @Test
    void findByCandidateAndJob_returnsEmpty_whenNotExists() {
        Optional<Application> found = applicationRepository.findByCandidateAndJob(savedCandidate, savedJob);
        assertThat(found).isEmpty();
    }

    @Test
    void existsByCandidateAndJob_returnsTrue_whenExists() {
        createApplication(savedCandidate, savedJob, ApplicationStatus.APPLIED);

        boolean exists = applicationRepository.existsByCandidateAndJob(savedCandidate, savedJob);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByCandidateAndJob_returnsFalse_whenNotExists() {
        boolean exists = applicationRepository.existsByCandidateAndJob(savedCandidate, savedJob);
        assertThat(exists).isFalse();
    }

    @Test
    void findByCandidateOrderByAppliedAtDesc_returnsApplications() {
        createApplication(savedCandidate, savedJob, ApplicationStatus.APPLIED);

        Page<Application> applications = applicationRepository.findByCandidateOrderByAppliedAtDesc(
                savedCandidate, PageRequest.of(0, 10));

        assertThat(applications.getContent()).hasSize(1);
    }

    @Test
    void findByJobRecruiterOrderByAppliedAtDesc_returnsApplications() {
        createApplication(savedCandidate, savedJob, ApplicationStatus.APPLIED);

        Page<Application> applications = applicationRepository.findByJobRecruiterOrderByAppliedAtDesc(
                savedRecruiter, PageRequest.of(0, 10));

        assertThat(applications.getContent()).hasSize(1);
    }

    @Test
    void findByJobAndJobRecruiterOrderByAppliedAtDesc_returnsApplications() {
        createApplication(savedCandidate, savedJob, ApplicationStatus.APPLIED);

        Page<Application> applications = applicationRepository.findByJobAndJobRecruiterOrderByAppliedAtDesc(
                savedJob, savedRecruiter, PageRequest.of(0, 10));

        assertThat(applications.getContent()).hasSize(1);
    }

    @Test
    void countByJobAndStatus_countsCorrectly() {
        createApplication(savedCandidate, savedJob, ApplicationStatus.APPLIED);

        long count = applicationRepository.countByJobAndStatus(savedJob, ApplicationStatus.APPLIED);

        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByJob_countsCorrectly() {
        createApplication(savedCandidate, savedJob, ApplicationStatus.APPLIED);

        long count = applicationRepository.countByJob(savedJob);

        assertThat(count).isEqualTo(1);
    }
}
