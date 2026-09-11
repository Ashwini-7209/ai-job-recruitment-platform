package com.jobplatform.matching;

import com.jobplatform.application.Application;
import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JobRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(JobRecommendationService.class);

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final JobMatchingService jobMatchingService;

    public JobRecommendationService(
            JobRepository jobRepository,
            ApplicationRepository applicationRepository,
            JobMatchingService jobMatchingService) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.jobMatchingService = jobMatchingService;
    }

    public RecommendationResult getRecommendations(User candidate, int page, int size,
                                                     Integer minScore) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Job> publishedJobs = jobRepository.findByStatus(JobStatus.PUBLISHED, pageable);

        Set<Long> appliedJobIds = applicationRepository
                .findByCandidateOrderByAppliedAtDesc(candidate, PageRequest.of(0, 1000))
                .getContent().stream()
                .filter(a -> a.getStatus() != ApplicationStatus.WITHDRAWN)
                .map(a -> a.getJob().getId())
                .collect(Collectors.toSet());

        List<JobMatchScore> scored = new ArrayList<>();
        for (Job job : publishedJobs.getContent()) {
            if (appliedJobIds.contains(job.getId())) {
                continue;
            }
            try {
                JobMatchingService.JobMatchResult match =
                        jobMatchingService.calculateMatch(candidate, job);
                if (minScore == null || match.getOverallScore() >= minScore) {
                    scored.add(JobMatchScore.builder()
                            .job(job)
                            .matchResult(match)
                            .build());
                }
            } catch (Exception e) {
                log.warn("Failed to calculate match for job {}: {}", job.getId(), e.getMessage());
            }
        }

        scored.sort(Comparator
                .comparingInt((JobMatchScore s) -> s.getMatchResult().getOverallScore()).reversed()
                .thenComparing(Comparator.comparing((JobMatchScore s) -> s.getJob().getCreatedAt(),
                        Comparator.nullsLast(Comparator.naturalOrder()))));

        List<JobMatchScore> pageContent = scored.stream()
                .skip((long) page * size)
                .limit(size)
                .toList();

        return RecommendationResult.builder()
                .matches(pageContent.stream().map(s -> s.getMatchResult()).toList())
                .totalElements(scored.size())
                .totalPages((int) Math.ceil((double) scored.size() / size))
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class JobMatchScore {
        private Job job;
        private JobMatchingService.JobMatchResult matchResult;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RecommendationResult {
        private List<JobMatchingService.JobMatchResult> matches;
        private int totalElements;
        private int totalPages;
        private int currentPage;
        private int pageSize;
    }
}
