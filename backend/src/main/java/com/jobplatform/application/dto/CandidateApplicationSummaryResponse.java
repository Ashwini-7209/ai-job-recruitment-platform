package com.jobplatform.application.dto;

import com.jobplatform.application.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateApplicationSummaryResponse {

    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String location;
    private String employmentType;
    private String workplaceType;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private Boolean hasResume;
    private String resumeFileName;
    private LocalDateTime deadline;
}
