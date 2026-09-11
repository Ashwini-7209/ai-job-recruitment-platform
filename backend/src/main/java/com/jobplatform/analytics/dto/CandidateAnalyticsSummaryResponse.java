package com.jobplatform.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAnalyticsSummaryResponse {

    private long totalApplications;
    private long applicationsUnderReview;
    private long applicationsShortlisted;
    private long applicationsRejected;
    private long applicationsHired;
    private long applicationsWithdrawn;
    private long totalInterviews;
    private long upcomingInterviews;
    private long totalSavedJobs;
    private long activeJobAlerts;
}
