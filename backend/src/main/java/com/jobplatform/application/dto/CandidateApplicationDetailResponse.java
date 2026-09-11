package com.jobplatform.application.dto;

import com.jobplatform.application.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateApplicationDetailResponse {

    private Long applicationId;
    private ApplicationStatus status;
    private String coverLetter;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime withdrawnAt;

    private Long jobId;
    private String jobTitle;
    private String jobDescription;
    private String location;
    private String employmentType;
    private String workplaceType;
    private Integer experienceMin;
    private Integer experienceMax;
    private Integer salaryMin;
    private Integer salaryMax;
    private String skills;
    private LocalDateTime deadline;

    private Long resumeId;
    private String resumeFileName;
    private String resumeContentType;
    private Long resumeFileSize;

    private List<StatusHistoryEntry> statusHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusHistoryEntry {
        private Long id;
        private ApplicationStatus oldStatus;
        private ApplicationStatus newStatus;
        private String changedByName;
        private LocalDateTime changedAt;
    }
}
