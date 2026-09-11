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
public class ApplicationResponse {

    private Long id;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private ApplicationStatus status;
    private String coverLetter;
    private Long submittedResumeId;
    private String submittedResumeName;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime withdrawnAt;
}
