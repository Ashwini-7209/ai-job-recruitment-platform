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
public class RecruiterApplicationSummaryResponse {

    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private String candidateHeadline;
    private String candidateEmail;
    private Long jobId;
    private String jobTitle;
    private ApplicationStatus status;
    private boolean hasResume;
    private String resumeFileName;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private int noteCount;
}
