package com.jobplatform.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerInsightsResponse {

    private List<String> strengths;
    private List<String> recommendedSkills;
    private List<String> suggestedJobCategories;
    private List<String> profileImprovements;
    private List<String> resumeImprovements;
    private List<String> generalCareerSuggestions;
    private boolean aiEnhanced;
}
