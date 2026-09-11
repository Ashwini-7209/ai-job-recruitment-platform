package com.jobplatform.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterJobAnalyticsResponse {

    private Long jobId;
    private String jobTitle;
    private String status;
    private long applicationCount;
    private long shortlistedCount;
    private long hiredCount;
    private long interviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
