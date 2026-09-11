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
public class AdminJobAnalyticsResponse {

    private long totalJobs;
    private long draftJobs;
    private long publishedJobs;
    private long closedJobs;
    private Map<String, Long> applicationsPerJob;
}
