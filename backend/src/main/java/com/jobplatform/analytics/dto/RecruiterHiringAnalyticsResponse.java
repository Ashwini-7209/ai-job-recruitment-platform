package com.jobplatform.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterHiringAnalyticsResponse {

    private long totalHired;
    private long totalShortlisted;
    private long totalInterviews;
    private long completedInterviews;
    private long cancelledInterviews;
    private double shortlistRate;
    private double hireRate;
}
