package com.jobplatform.analytics;

import com.jobplatform.analytics.dto.CandidateAnalyticsSummaryResponse;
import com.jobplatform.analytics.dto.CandidateApplicationStatusResponse;
import com.jobplatform.analytics.dto.TrendResponse;
import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.interview.InterviewRepository;
import com.jobplatform.jobalert.JobAlertRepository;
import com.jobplatform.savedjob.SavedJobRepository;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CandidateAnalyticsService {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobAlertRepository jobAlertRepository;

    public CandidateAnalyticsService(ApplicationRepository applicationRepository,
                                      InterviewRepository interviewRepository,
                                      SavedJobRepository savedJobRepository,
                                      JobAlertRepository jobAlertRepository) {
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.savedJobRepository = savedJobRepository;
        this.jobAlertRepository = jobAlertRepository;
    }

    @Transactional(readOnly = true)
    public CandidateAnalyticsSummaryResponse getSummary(User candidate) {
        long totalApplications = applicationRepository.countByCandidate(candidate);
        long underReview = applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.UNDER_REVIEW);
        long shortlisted = applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.SHORTLISTED);
        long rejected = applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.REJECTED);
        long hired = applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.HIRED);
        long withdrawn = applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.WITHDRAWN);
        long totalInterviews = interviewRepository.countByApplicationCandidate(candidate);
        long upcomingInterviews = interviewRepository.countByApplicationCandidateAndScheduledStartAfter(
                candidate, LocalDateTime.now());
        long savedJobs = savedJobRepository.countByCandidate(candidate);
        long activeAlerts = jobAlertRepository.countByCandidateAndActiveTrue(candidate);

        return CandidateAnalyticsSummaryResponse.builder()
                .totalApplications(totalApplications)
                .applicationsUnderReview(underReview)
                .applicationsShortlisted(shortlisted)
                .applicationsRejected(rejected)
                .applicationsHired(hired)
                .applicationsWithdrawn(withdrawn)
                .totalInterviews(totalInterviews)
                .upcomingInterviews(upcomingInterviews)
                .totalSavedJobs(savedJobs)
                .activeJobAlerts(activeAlerts)
                .build();
    }

    @Transactional(readOnly = true)
    public CandidateApplicationStatusResponse getApplicationStatusDistribution(User candidate) {
        List<Object[]> results = applicationRepository.countStatusDistributionByCandidate(candidate);
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            distribution.put(status.name(), 0L);
        }
        for (Object[] row : results) {
            ApplicationStatus status = (ApplicationStatus) row[0];
            Long count = (Long) row[1];
            distribution.put(status.name(), count);
        }
        return CandidateApplicationStatusResponse.builder()
                .statusDistribution(distribution)
                .build();
    }

    @Transactional(readOnly = true)
    public TrendResponse getApplicationTrend(User candidate, LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<Object[]> results = applicationRepository.countByCandidateAndDateRange(candidate, fromDateTime, toDateTime);
        List<TrendResponse.TrendPoint> data = results.stream()
                .map(row -> TrendResponse.TrendPoint.builder()
                        .date(row[0].toString())
                        .count((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return TrendResponse.builder().data(data).build();
    }
}
