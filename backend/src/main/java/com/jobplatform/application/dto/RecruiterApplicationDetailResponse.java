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
public class RecruiterApplicationDetailResponse {

    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private ApplicationStatus status;
    private String coverLetter;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    private Long resumeId;
    private String resumeFileName;
    private String resumeContentType;
    private Long resumeFileSize;

    private CandidateInfo candidate;
    private List<StatusHistoryEntry> statusHistory;
    private List<ApplicationNoteResponse> notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateInfo {
        private Long candidateId;
        private String fullName;
        private String email;
        private String headline;
        private String location;
        private String bio;
        private String currentJobTitle;
        private Integer yearsOfExperience;
        private String skillsSummary;
        private String educationSummary;
        private String linkedinUrl;
        private String githubUrl;
        private String portfolioUrl;

        private String parsedSkills;
        private Integer parsedYearsOfExperience;
        private String professionalSummary;
    }

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
        private String reason;
    }
}
