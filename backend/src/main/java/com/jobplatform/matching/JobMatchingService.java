package com.jobplatform.matching;

import com.jobplatform.job.Job;
import com.jobplatform.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class JobMatchingService {

    private static final Logger log = LoggerFactory.getLogger(JobMatchingService.class);

    private final CandidateDataResolver candidateDataResolver;
    private final JobRequirementResolver jobRequirementResolver;
    private final RuleBasedMatchingEngine matchingEngine;
    private final AIMatchingService aiMatchingService;

    public JobMatchingService(
            CandidateDataResolver candidateDataResolver,
            JobRequirementResolver jobRequirementResolver,
            RuleBasedMatchingEngine matchingEngine,
            AIMatchingService aiMatchingService) {
        this.candidateDataResolver = candidateDataResolver;
        this.jobRequirementResolver = jobRequirementResolver;
        this.matchingEngine = matchingEngine;
        this.aiMatchingService = aiMatchingService;
    }

    public JobMatchResult calculateMatch(User candidate, Job job) {
        CandidateData candidateData = candidateDataResolver.resolve(candidate);
        JobData jobData = jobRequirementResolver.resolve(job);

        RuleBasedMatchingEngine.DeterministicScore deterministicScore =
                matchingEngine.calculate(candidateData, jobData);

        int finalScore = deterministicScore.getOverallScore();
        Integer semanticScore = null;
        List<String> aiReasons = new ArrayList<>();
        List<String> aiGaps = new ArrayList<>();
        List<String> aiEvidence = new ArrayList<>();
        String aiExplanation = null;
        boolean aiUsed = false;

        try {
            Optional<AIMatchingService.AISemanticResult> aiResult =
                    aiMatchingService.evaluateMatch(candidateData, jobData, candidate.getId());
            if (aiResult.isPresent()) {
                AIMatchingService.AISemanticResult ai = aiResult.get();
                semanticScore = ai.getRelevanceScore();
                aiReasons = ai.getMatchingReasons() != null ? ai.getMatchingReasons() : new ArrayList<>();
                aiGaps = ai.getPotentialGaps() != null ? ai.getPotentialGaps() : new ArrayList<>();
                aiEvidence = ai.getEvidence() != null ? ai.getEvidence() : new ArrayList<>();
                aiUsed = true;

                finalScore = (int) Math.round(
                        deterministicScore.getOverallScore() * RuleBasedMatchingEngine.AI_ENHANCEMENT_WEIGHT
                        + semanticScore * (1.0 - RuleBasedMatchingEngine.AI_ENHANCEMENT_WEIGHT));
                finalScore = Math.max(0, Math.min(100, finalScore));
            }
        } catch (Exception e) {
            log.warn("AI matching failed for job {}, falling back to deterministic: {}",
                    job.getId(), e.getMessage());
        }

        try {
            aiExplanation = aiMatchingService.buildMatchExplanation(candidateData, jobData);
        } catch (Exception e) {
            log.warn("AI explanation failed for job {}: {}", job.getId(), e.getMessage());
        }

        List<String> allReasons = new ArrayList<>(deterministicScore.getMatchingReasons());
        allReasons.addAll(aiReasons);

        List<String> allGaps = new ArrayList<>(deterministicScore.getPotentialGaps());
        allGaps.addAll(aiGaps);

        return JobMatchResult.builder()
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .overallScore(finalScore)
                .skillScore(deterministicScore.getSkillScore())
                .experienceScore(deterministicScore.getExperienceScore())
                .profileScore(deterministicScore.getProfileScore())
                .semanticScore(semanticScore)
                .matchedSkills(deterministicScore.getMatchedSkills())
                .missingSkills(deterministicScore.getMissingSkills())
                .matchingReasons(allReasons)
                .potentialGaps(allGaps)
                .aiEvidence(aiEvidence)
                .aiExplanation(aiExplanation)
                .aiUsed(aiUsed)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class JobMatchResult {
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
        private List<String> aiEvidence;
        private String aiExplanation;
        private boolean aiUsed;
    }
}
