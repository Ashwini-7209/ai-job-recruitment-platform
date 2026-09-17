package com.jobplatform.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateSearchResultResponse {

    private Long candidateId;
    private Long userId;
    private String fullName;
    private String email;
    private String headline;
    private String location;
    private String currentJobTitle;
    private Integer yearsOfExperience;
    private String skillsSummary;
    private String educationSummary;
    private String bio;
    private String profileImageUrl;
    private String linkedinUrl;
    private String githubUrl;
    private boolean hasResume;
}
