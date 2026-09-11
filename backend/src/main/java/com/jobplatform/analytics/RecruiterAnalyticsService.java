package com.jobplatform.analytics;

import com.jobplatform.analytics.dto.RecruiterAnalyticsSummaryResponse;
import com.jobplatform.analytics.dto.RecruiterFunnelResponse;
import com.jobplatform.analytics.dto.RecruiterHiringAnalyticsResponse;
import com.jobplatform.analytics.dto.RecruiterJobAnalyticsResponse;
import com.jobplatform.analytics.dto.TrendResponse;
import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.interview.InterviewRepository;
import com.jobplatform.interview.enums.InterviewStatus;
import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecruiterAnalyticsService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;

    public RecruiterAnalyticsService(JobRepository jobRepository,
                                      ApplicationRepository applicationRepository,
                                      InterviewRepository interviewRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
    }

    @Transactional(readOnly = true)
    public RecruiterAnalyticsSummaryResponse getSummary(User recruiter) {
        long totalJobs = jobRepository.countByRecruiter(recruiter);
        long publishedJobs = jobRepository.countByRecruiterAndStatus(recruiter, JobStatus.PUBLISHED);
        long closedJobs = jobRepository.countByRecruiterAndStatus(recruiter, JobStatus.CLOSED);
        long totalApplications = applicationRepository.countByJobRecruiter(recruiter);
        long underReview = applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.UNDER_REVIEW);
        long shortlisted = applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.SHORTLISTED);
        long rejected = applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.REJECTED);
        long hired = applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.HIRED);
        long upcomingInterviews = interviewRepository.countByApplicationJobRecruiterAndScheduledStartAfter(
                recruiter, LocalDateTime.now());
        long completedInterviews = interviewRepository.countByApplicationJobRecruiterAndStatus(
                recruiter, InterviewStatus.COMPLETED);

        return RecruiterAnalyticsSummaryResponse.builder()
                .totalJobs(totalJobs)
                .publishedJobs(publishedJobs)
                .closedJobs(closedJobs)
                .totalApplications(totalApplications)
                .applicationsUnderReview(underReview)
                .shortlistedCandidates(shortlisted)
                .rejectedCandidates(rejected)
                .hiredCandidates(hired)
                .upcomingInterviews(upcomingInterviews)
                .completedInterviews(completedInterviews)
                .build();
    }

    @Transactional(readOnly = true)
    public RecruiterFunnelResponse getFunnel(User recruiter) {
        List<Object[]> results = applicationRepository.countStatusDistributionByRecruiter(recruiter);
        Map<String, Long> funnel = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            funnel.put(status.name(), 0L);
        }
        for (Object[] row : results) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            Long count = (Long) row[1];
            funnel.put(status.name(), count);
        }
        return RecruiterFunnelResponse.builder().funnel(funnel).build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<RecruiterJobAnalyticsResponse> getJobAnalytics(User recruiter, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Job> jobs = jobRepository.findByRecruiterOrdered(recruiter, pageable);

        List<RecruiterJobAnalyticsResponse> content = jobs.getContent().stream()
                .map(job -> {
                    long appCount = applicationRepository.countByJob(job);
                    long shortlisted = applicationRepository.countByJobAndStatus(job, ApplicationStatus.SHORTLISTED);
                    long hired = applicationRepository.countByJobAndStatus(job, ApplicationStatus.HIRED);
                    // Interview count for this job's applications
                    long interviewCount = 0; // Would need a custom query for this

                    return RecruiterJobAnalyticsResponse.builder()
                            .jobId(job.getId())
                            .jobTitle(job.getTitle())
                            .status(job.getStatus().name())
                            .applicationCount(appCount)
                            .shortlistedCount(shortlisted)
                            .hiredCount(hired)
                            .interviewCount(interviewCount)
                            .createdAt(job.getCreatedAt())
                            .publishedAt(job.getPublishedAt())
                            .build();
                })
                .toList();

        return PagedResponse.<RecruiterJobAnalyticsResponse>builder()
                .content(content)
                .page(jobs.getNumber())
                .size(jobs.getSize())
                .totalElements(jobs.getTotalElements())
                .totalPages(jobs.getTotalPages())
                .last(jobs.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public TrendResponse getApplicationTrend(User recruiter, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<Object[]> results = applicationRepository.countByRecruiterAndDateRange(recruiter, fromDateTime, toDateTime);
        List<TrendResponse.TrendPoint> data = results.stream()
                .map(row -> TrendResponse.TrendPoint.builder()
                        .date(row[0].toString())
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return TrendResponse.builder().data(data).build();
    }

    @Transactional(readOnly = true)
    public RecruiterHiringAnalyticsResponse getHiringMetrics(User recruiter) {
        long totalApplications = applicationRepository.countByJobRecruiter(recruiter);
        long shortlisted = applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.SHORTLISTED);
        long hired = applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.HIRED);
        long totalInterviews = interviewRepository.countByApplicationJobRecruiter(recruiter);
        long completedInterviews = interviewRepository.countByApplicationJobRecruiterAndStatus(
                recruiter, InterviewStatus.COMPLETED);
        long cancelledInterviews = interviewRepository.countByApplicationJobRecruiterAndStatus(
                recruiter, InterviewStatus.CANCELLED);

        double shortlistRate = totalApplications > 0 ? (double) shortlisted / totalApplications * 100 : 0;
        double hireRate = totalApplications > 0 ? (double) hired / totalApplications * 100 : 0;

        return RecruiterHiringAnalyticsResponse.builder()
                .totalHired(hired)
                .totalShortlisted(shortlisted)
                .totalInterviews(totalInterviews)
                .completedInterviews(completedInterviews)
                .cancelledInterviews(cancelledInterviews)
                .shortlistRate(Math.round(shortlistRate * 100.0) / 100.0)
                .hireRate(Math.round(hireRate * 100.0) / 100.0)
                .build();
    }
}
