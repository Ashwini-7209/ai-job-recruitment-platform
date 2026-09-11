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
public class ResumeImprovementResponse {

    private Long resumeId;
    private int completeness;
    private List<String> strengths;
    private List<String> improvements;
    private List<String> missingSections;
    private List<String> keywordSuggestions;
    private boolean aiEnhanced;
}
