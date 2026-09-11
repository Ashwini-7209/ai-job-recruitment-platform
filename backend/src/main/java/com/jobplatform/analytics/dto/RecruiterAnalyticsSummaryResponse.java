package com.jobplatform.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterAnalyticsSummaryResponse {

    private long totalJobs;
    private long publishedJobs;
    private long closedJobs;
    private long totalApplications;
    private long applicationsUnderReview;
    private long shortlistedCandidates;
    private long rejectedCandidates;
    private long hiredCandidates;
    private long upcomingInterviews;
    private long completedInterviews;
}
