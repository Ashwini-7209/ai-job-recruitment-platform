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
public class JobMatchResponse {

    private Long jobId;
    private String jobTitle;
    private int overallScore;
    private int skillScore;
    private int experienceScore;
    private int profileScore;
    private Integer semanticScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private List<String> matchingReasons;
    private List<String> potentialGaps;
    private String aiExplanation;
    private boolean aiUsed;

    public static JobMatchResponse from(com.jobplatform.matching.JobMatchingService.JobMatchResult result) {
        return JobMatchResponse.builder()
                .jobId(result.getJobId())
                .jobTitle(result.getJobTitle())
                .overallScore(result.getOverallScore())
                .skillScore(result.getSkillScore())
                .experienceScore(result.getExperienceScore())
                .profileScore(result.getProfileScore())
                .semanticScore(result.getSemanticScore())
                .matchedSkills(result.getMatchedSkills())
                .missingSkills(result.getMissingSkills())
                .matchingReasons(result.getMatchingReasons())
                .potentialGaps(result.getPotentialGaps())
                .aiExplanation(result.getAiExplanation())
                .aiUsed(result.isAiUsed())
                .build();
    }
}
