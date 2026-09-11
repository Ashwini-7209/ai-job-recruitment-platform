package com.jobplatform.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminInterviewAnalyticsResponse {

    private long totalInterviews;
    private long scheduled;
    private long rescheduled;
    private long completed;
    private long cancelled;
    private Map<String, Long> statusDistribution;
}
