package com.jobplatform.job;

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
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JobRepositoryTests {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User savedRecruiter;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .fullName("Test Recruiter")
                .email("recruiter-" + System.nanoTime() + "@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.RECRUITER)
                .enabled(true)
                .build();
        savedRecruiter = userRepository.save(user);
    }

    private Job createJob(String title, JobStatus status) {
        Job job = Job.builder()
                .recruiter(savedRecruiter)
                .title(title)
                .description("Description for " + title)
                .location("New York")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.REMOTE)
                .status(status)
                .build();
        return jobRepository.save(job);
    }

    @Test
    void save_createsJob() {
        Job job = Job.builder()
                .recruiter(savedRecruiter)
                .title("Software Engineer")
                .description("We are looking for a software engineer")
                .location("San Francisco")
                .employmentType(EmploymentType.FULL_TIME)
                .workplaceType(WorkplaceType.HYBRID)
                .experienceMin(2)
                .experienceMax(5)
                .salaryMin(80000)
                .salaryMax(120000)
                .skills("Java, Spring Boot, React")
                .status(JobStatus.DRAFT)
                .build();

        Job saved = jobRepository.save(job);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Software Engineer");
        assertThat(saved.getRecruiter().getId()).isEqualTo(savedRecruiter.getId());
        assertThat(saved.getStatus()).isEqualTo(JobStatus.DRAFT);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByRecruiter_returnsJobs() {
        createJob("Job 1", JobStatus.DRAFT);
        createJob("Job 2", JobStatus.PUBLISHED);

        Page<Job> jobs = jobRepository.findByRecruiter(savedRecruiter, PageRequest.of(0, 10));

        assertThat(jobs.getContent()).hasSize(2);
    }

    @Test
    void findByRecruiterAndStatus_filtersByStatus() {
        createJob("Draft Job", JobStatus.DRAFT);
        createJob("Published Job", JobStatus.PUBLISHED);
        createJob("Another Draft", JobStatus.DRAFT);

        Page<Job> draftJobs = jobRepository.findByRecruiterAndStatus(savedRecruiter, JobStatus.DRAFT, PageRequest.of(0, 10));

        assertThat(draftJobs.getContent()).hasSize(2);
    }

    @Test
    void countByRecruiter_countsCorrectly() {
        createJob("Job 1", JobStatus.DRAFT);
        createJob("Job 2", JobStatus.PUBLISHED);

        long count = jobRepository.countByRecruiter(savedRecruiter);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByRecruiterAndStatus_countsCorrectly() {
        createJob("Draft 1", JobStatus.DRAFT);
        createJob("Draft 2", JobStatus.DRAFT);
        createJob("Published 1", JobStatus.PUBLISHED);

        long draftCount = jobRepository.countByRecruiterAndStatus(savedRecruiter, JobStatus.DRAFT);
        long publishedCount = jobRepository.countByRecruiterAndStatus(savedRecruiter, JobStatus.PUBLISHED);

        assertThat(draftCount).isEqualTo(2);
        assertThat(publishedCount).isEqualTo(1);
    }

    @Test
    void searchPublishedJobs_findsByTitle() {
        Job job = createJob("Java Developer", JobStatus.PUBLISHED);

        Page<Job> results = jobRepository.searchPublishedJobs("Java", PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("Java Developer");
    }

    @Test
    void searchPublishedJobs_findsBySkills() {
        Job job = createJob("Developer", JobStatus.PUBLISHED);
        job.setSkills("Python, Django");
        jobRepository.save(job);

        Page<Job> results = jobRepository.searchPublishedJobs("Python", PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(1);
    }

    @Test
    void searchPublishedJobs_excludesDraftJobs() {
        createJob("Java Developer", JobStatus.DRAFT);

        Page<Job> results = jobRepository.searchPublishedJobs("Java", PageRequest.of(0, 10));

        assertThat(results.getContent()).isEmpty();
    }

    @Test
    void findPublishedJobsWithFilters_filtersByLocation() {
        Job job1 = createJob("Job 1", JobStatus.PUBLISHED);
        job1.setLocation("New York");
        jobRepository.save(job1);

        Job job2 = createJob("Job 2", JobStatus.PUBLISHED);
        job2.setLocation("San Francisco");
        jobRepository.save(job2);

        Page<Job> results = jobRepository.findPublishedJobsWithFilters(
                "New York", null, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getLocation()).isEqualTo("New York");
    }

    @Test
    void findPublishedJobsWithFilters_filtersByEmploymentType() {
        Job job1 = createJob("Full Time Job", JobStatus.PUBLISHED);
        job1.setEmploymentType(EmploymentType.FULL_TIME);
        jobRepository.save(job1);

        Job job2 = createJob("Part Time Job", JobStatus.PUBLISHED);
        job2.setEmploymentType(EmploymentType.PART_TIME);
        jobRepository.save(job2);

        Page<Job> results = jobRepository.findPublishedJobsWithFilters(
                null, EmploymentType.FULL_TIME, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getEmploymentType()).isEqualTo(EmploymentType.FULL_TIME);
    }

    @Test
    void findPublishedJobsWithFilters_excludesDraftJobs() {
        createJob("Draft Job", JobStatus.DRAFT);

        Page<Job> results = jobRepository.findPublishedJobsWithFilters(
                null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(results.getContent()).isEmpty();
    }
}
