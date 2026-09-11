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
public class SkillGapResponse {

    private Long jobId;
    private String jobTitle;
    private List<String> jobRequiredSkills;
    private List<String> candidateSkills;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> partiallyMatchedSkills;
    private List<String> prioritySuggestions;
    private String explanation;
    private boolean aiEnhanced;
}
