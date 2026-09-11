package com.jobplatform.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsSummaryResponse {

    private long totalUsers;
    private long totalCandidates;
    private long totalRecruiters;
    private long activeUsers;
    private long totalJobs;
    private long publishedJobs;
    private long closedJobs;
    private long totalApplications;
    private long totalInterviews;
    private long activeJobAlerts;
    private long totalSavedJobs;
}
