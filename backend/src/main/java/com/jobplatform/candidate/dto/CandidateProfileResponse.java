package com.jobplatform.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String location;
    private String headline;
    private String bio;
    private String currentJobTitle;
    private Integer yearsOfExperience;
    private String educationSummary;
    private String skillsSummary;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
