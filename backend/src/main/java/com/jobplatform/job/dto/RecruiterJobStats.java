package com.jobplatform.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterJobStats {

    private long totalJobs;
    private long draftJobs;
    private long publishedJobs;
    private long closedJobs;
}
