package com.jobplatform.analytics;

import com.jobplatform.analytics.dto.AdminAnalyticsSummaryResponse;
import com.jobplatform.analytics.dto.AdminApplicationAnalyticsResponse;
import com.jobplatform.analytics.dto.AdminInterviewAnalyticsResponse;
import com.jobplatform.analytics.dto.AdminJobAnalyticsResponse;
import com.jobplatform.analytics.dto.TrendResponse;
import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.interview.InterviewRepository;
import com.jobplatform.interview.enums.InterviewStatus;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.jobalert.JobAlertRepository;
import com.jobplatform.savedjob.SavedJobRepository;
import com.jobplatform.user.UserRole;
import com.jobplatform.user.UserRepository;
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
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobAlertRepository jobAlertRepository;

    public AdminAnalyticsService(UserRepository userRepository,
                                  JobRepository jobRepository,
                                  ApplicationRepository applicationRepository,
                                  InterviewRepository interviewRepository,
                                  SavedJobRepository savedJobRepository,
                                  JobAlertRepository jobAlertRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.savedJobRepository = savedJobRepository;
        this.jobAlertRepository = jobAlertRepository;
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsSummaryResponse getSummary() {
        long totalUsers = userRepository.count();
        long totalCandidates = userRepository.countByRole(UserRole.CANDIDATE);
        long totalRecruiters = userRepository.countByRole(UserRole.RECRUITER);
        long activeUsers = userRepository.countByEnabled(true);
        long totalJobs = jobRepository.count();
        long publishedJobs = jobRepository.countByStatus(JobStatus.PUBLISHED);
        long closedJobs = jobRepository.countByStatus(JobStatus.CLOSED);
        long totalApplications = applicationRepository.count();
        long totalInterviews = interviewRepository.count();

        return AdminAnalyticsSummaryResponse.builder()
                .totalUsers(totalUsers)
                .totalCandidates(totalCandidates)
                .totalRecruiters(totalRecruiters)
                .activeUsers(activeUsers)
                .totalJobs(totalJobs)
                .publishedJobs(publishedJobs)
                .closedJobs(closedJobs)
                .totalApplications(totalApplications)
                .totalInterviews(totalInterviews)
                .activeJobAlerts(0)
                .totalSavedJobs(0)
                .build();
    }

    @Transactional(readOnly = true)
    public TrendResponse getUserGrowth(LocalDate from, LocalDate to, UserRole roleFilter) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<Object[]> results;
        if (roleFilter != null) {
            results = userRepository.countByDateRangeAndRole(fromDateTime, toDateTime, roleFilter);
        } else {
            results = userRepository.countByDateRange(fromDateTime, toDateTime);
        }
        List<TrendResponse.TrendPoint> data = results.stream()
                .map(row -> TrendResponse.TrendPoint.builder()
                        .date(row[0].toString())
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return TrendResponse.builder().data(data).build();
    }

    @Transactional(readOnly = true)
    public AdminJobAnalyticsResponse getJobAnalytics() {
        long totalJobs = jobRepository.count();
        long draftJobs = jobRepository.countByStatus(JobStatus.DRAFT);
        long publishedJobs = jobRepository.countByStatus(JobStatus.PUBLISHED);
        long closedJobs = jobRepository.countByStatus(JobStatus.CLOSED);

        return AdminJobAnalyticsResponse.builder()
                .totalJobs(totalJobs)
                .draftJobs(draftJobs)
                .publishedJobs(publishedJobs)
                .closedJobs(closedJobs)
                .applicationsPerJob(new LinkedHashMap<>())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminApplicationAnalyticsResponse getApplicationAnalytics() {
        long totalApplications = applicationRepository.count();
        List<Object[]> results = applicationRepository.countAllStatusDistribution();

        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusDistribution.put(status.name(), 0L);
        }
        for (Object[] row : results) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            Long count = (Long) row[1];
            statusDistribution.put(status.name(), count);
        }

        return AdminApplicationAnalyticsResponse.builder()
                .totalApplications(totalApplications)
                .statusDistribution(statusDistribution)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminInterviewAnalyticsResponse getInterviewAnalytics() {
        long totalInterviews = interviewRepository.count();
        List<Object[]> results = interviewRepository.countAllStatusDistribution();

        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        for (InterviewStatus status : InterviewStatus.values()) {
            statusDistribution.put(status.name(), 0L);
        }
        long scheduled = 0;
        long rescheduled = 0;
        long completed = 0;
        long cancelled = 0;

        for (Object[] row : results) {
            InterviewStatus status = (InterviewStatus) row[0];
            Long count = (Long) row[1];
            statusDistribution.put(status.name(), count);
            switch (status) {
                case SCHEDULED -> scheduled = count;
                case RESCHEDULED -> rescheduled = count;
                case COMPLETED -> completed = count;
                case CANCELLED -> cancelled = count;
            }
        }

        return AdminInterviewAnalyticsResponse.builder()
                .totalInterviews(totalInterviews)
                .scheduled(scheduled)
                .rescheduled(rescheduled)
                .completed(completed)
                .cancelled(cancelled)
                .statusDistribution(statusDistribution)
                .build();
    }

    @Transactional(readOnly = true)
    public TrendResponse getApplicationTrend(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<Object[]> results = applicationRepository.countByDateRange(fromDateTime, toDateTime);
        List<TrendResponse.TrendPoint> data = results.stream()
                .map(row -> TrendResponse.TrendPoint.builder()
                        .date(row[0].toString())
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return TrendResponse.builder().data(data).build();
    }
}
