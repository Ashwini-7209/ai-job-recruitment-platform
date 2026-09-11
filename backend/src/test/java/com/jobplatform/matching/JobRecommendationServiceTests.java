package com.jobplatform.matching;

import com.jobplatform.application.Application;
import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobRecommendationServiceTests {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobMatchingService jobMatchingService;

    @InjectMocks
    private JobRecommendationService recommendationService;

    private User candidate;
    private Job job1;
    private Job job2;
    private Job job3;

    @BeforeEach
    void setUp() {
        candidate = User.builder()
                .id(1L)
                .email("candidate@example.com")
                .role(UserRole.CANDIDATE)
                .build();

        User recruiter = User.builder().id(2L).role(UserRole.RECRUITER).build();

        job1 = Job.builder().id(10L).title("Java Developer").status(JobStatus.PUBLISHED)
                .recruiter(recruiter).createdAt(LocalDateTime.now().minusDays(1)).build();
        job2 = Job.builder().id(11L).title("Python Developer").status(JobStatus.PUBLISHED)
                .recruiter(recruiter).createdAt(LocalDateTime.now().minusDays(2)).build();
        job3 = Job.builder().id(12L).title("DevOps Engineer").status(JobStatus.PUBLISHED)
                .recruiter(recruiter).createdAt(LocalDateTime.now()).build();
    }

    @Test
    void getRecommendations_onlyPublishedJobsReturned() {
        Page<Job> publishedJobs = new PageImpl<>(List.of(job1, job2, job3));
        when(jobRepository.findByStatus(eq(JobStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(publishedJobs);
        when(applicationRepository.findByCandidateOrderByAppliedAtDesc(eq(candidate), any(Pageable.class)))
                .thenReturn(Page.empty());

        when(jobMatchingService.calculateMatch(candidate, job1))
                .thenReturn(buildMatchResult(10L, "Java Developer", 80));
        when(jobMatchingService.calculateMatch(candidate, job2))
                .thenReturn(buildMatchResult(11L, "Python Developer", 60));
        when(jobMatchingService.calculateMatch(candidate, job3))
                .thenReturn(buildMatchResult(12L, "DevOps Engineer", 40));

        var result = recommendationService.getRecommendations(candidate, 0, 10, null);

        assertThat(result.getMatches()).hasSize(3);
        assertThat(result.getMatches().get(0).getOverallScore())
                .isGreaterThanOrEqualTo(result.getMatches().get(1).getOverallScore());
    }

    @Test
    void getRecommendations_rankedByScore() {
        Page<Job> publishedJobs = new PageImpl<>(List.of(job1, job2, job3));
        when(jobRepository.findByStatus(eq(JobStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(publishedJobs);
        when(applicationRepository.findByCandidateOrderByAppliedAtDesc(eq(candidate), any(Pageable.class)))
                .thenReturn(Page.empty());

        when(jobMatchingService.calculateMatch(candidate, job1))
                .thenReturn(buildMatchResult(10L, "Java Developer", 60));
        when(jobMatchingService.calculateMatch(candidate, job2))
                .thenReturn(buildMatchResult(11L, "Python Developer", 90));
        when(jobMatchingService.calculateMatch(candidate, job3))
                .thenReturn(buildMatchResult(12L, "DevOps Engineer", 40));

        var result = recommendationService.getRecommendations(candidate, 0, 10, null);

        assertThat(result.getMatches()).hasSize(3);
        assertThat(result.getMatches().get(0).getOverallScore()).isEqualTo(90);
        assertThat(result.getMatches().get(1).getOverallScore()).isEqualTo(60);
        assertThat(result.getMatches().get(2).getOverallScore()).isEqualTo(40);
    }

    @Test
    void getRecommendations_paginationWorks() {
        Page<Job> publishedJobs = new PageImpl<>(List.of(job1, job2, job3));
        when(jobRepository.findByStatus(eq(JobStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(publishedJobs);
        when(applicationRepository.findByCandidateOrderByAppliedAtDesc(eq(candidate), any(Pageable.class)))
                .thenReturn(Page.empty());

        when(jobMatchingService.calculateMatch(candidate, job1))
                .thenReturn(buildMatchResult(10L, "Java Developer", 80));
        when(jobMatchingService.calculateMatch(candidate, job2))
                .thenReturn(buildMatchResult(11L, "Python Developer", 60));
        when(jobMatchingService.calculateMatch(candidate, job3))
                .thenReturn(buildMatchResult(12L, "DevOps Engineer", 40));

        var page1 = recommendationService.getRecommendations(candidate, 0, 2, null);
        var page2 = recommendationService.getRecommendations(candidate, 1, 2, null);

        assertThat(page1.getMatches()).hasSize(2);
        assertThat(page2.getMatches()).hasSize(1);
        assertThat(page1.getTotalElements()).isEqualTo(3);
    }

    @Test
    void getRecommendations_excludesAppliedJobs() {
        Page<Job> publishedJobs = new PageImpl<>(List.of(job1, job2));
        when(jobRepository.findByStatus(eq(JobStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(publishedJobs);

        Application app = Application.builder()
                .candidate(candidate)
                .job(job1)
                .status(ApplicationStatus.APPLIED)
                .build();
        when(applicationRepository.findByCandidateOrderByAppliedAtDesc(eq(candidate), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(app)));

        when(jobMatchingService.calculateMatch(candidate, job2))
                .thenReturn(buildMatchResult(11L, "Python Developer", 70));

        var result = recommendationService.getRecommendations(candidate, 0, 10, null);

        assertThat(result.getMatches()).hasSize(1);
        assertThat(result.getMatches().get(0).getJobId()).isEqualTo(11L);
    }

    @Test
    void getRecommendations_withMinScore() {
        Page<Job> publishedJobs = new PageImpl<>(List.of(job1, job2, job3));
        when(jobRepository.findByStatus(eq(JobStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(publishedJobs);
        when(applicationRepository.findByCandidateOrderByAppliedAtDesc(eq(candidate), any(Pageable.class)))
                .thenReturn(Page.empty());

        when(jobMatchingService.calculateMatch(candidate, job1))
                .thenReturn(buildMatchResult(10L, "Java Developer", 80));
        when(jobMatchingService.calculateMatch(candidate, job2))
                .thenReturn(buildMatchResult(11L, "Python Developer", 50));
        when(jobMatchingService.calculateMatch(candidate, job3))
                .thenReturn(buildMatchResult(12L, "DevOps Engineer", 30));

        var result = recommendationService.getRecommendations(candidate, 0, 10, 60);

        assertThat(result.getMatches()).hasSize(1);
        assertThat(result.getMatches().get(0).getOverallScore()).isGreaterThanOrEqualTo(60);
    }

    @Test
    void getRecommendations_emptyResult() {
        Page<Job> publishedJobs = Page.empty();
        when(jobRepository.findByStatus(eq(JobStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(publishedJobs);
        when(applicationRepository.findByCandidateOrderByAppliedAtDesc(eq(candidate), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of()));

        var result = recommendationService.getRecommendations(candidate, 0, 10, null);

        assertThat(result.getMatches()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void getRecommendations_noDuplicateJobs() {
        Page<Job> publishedJobs = new PageImpl<>(List.of(job1, job2, job3));
        when(jobRepository.findByStatus(eq(JobStatus.PUBLISHED), any(Pageable.class)))
                .thenReturn(publishedJobs);
        when(applicationRepository.findByCandidateOrderByAppliedAtDesc(eq(candidate), any(Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of()));

        when(jobMatchingService.calculateMatch(candidate, job1))
                .thenReturn(buildMatchResult(10L, "Java Developer", 80));
        when(jobMatchingService.calculateMatch(candidate, job2))
                .thenReturn(buildMatchResult(11L, "Python Developer", 70));
        when(jobMatchingService.calculateMatch(candidate, job3))
                .thenReturn(buildMatchResult(12L, "DevOps Engineer", 60));

        var result = recommendationService.getRecommendations(candidate, 0, 10, null);

        java.util.Set<Long> ids = new java.util.HashSet<>();
        for (var match : result.getMatches()) {
            ids.add(match.getJobId());
        }
        assertThat(ids).hasSize(result.getMatches().size());
    }

    private JobMatchingService.JobMatchResult buildMatchResult(Long jobId, String title, int score) {
        return JobMatchingService.JobMatchResult.builder()
                .jobId(jobId)
                .jobTitle(title)
                .overallScore(score)
                .matchedSkills(List.of("java"))
                .missingSkills(List.of())
                .matchingReasons(List.of("Match"))
                .potentialGaps(List.of())
                .build();
    }
}
