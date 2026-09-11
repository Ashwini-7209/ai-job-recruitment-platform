package com.jobplatform.matching;

import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.matching.dto.SkillGapResponse;
import com.jobplatform.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SkillGapService {

    private final CandidateDataResolver candidateDataResolver;
    private final JobRequirementResolver jobRequirementResolver;
    private final RuleBasedMatchingEngine matchingEngine;
    private final AIMatchingService aiMatchingService;
    private final JobRepository jobRepository;

    public SkillGapService(
            CandidateDataResolver candidateDataResolver,
            JobRequirementResolver jobRequirementResolver,
            RuleBasedMatchingEngine matchingEngine,
            AIMatchingService aiMatchingService,
            JobRepository jobRepository) {
        this.candidateDataResolver = candidateDataResolver;
        this.jobRequirementResolver = jobRequirementResolver;
        this.matchingEngine = matchingEngine;
        this.aiMatchingService = aiMatchingService;
        this.jobRepository = jobRepository;
    }

    public SkillGapResponse analyzeSkillGap(User candidate, Job job) {
        CandidateData candidateData = candidateDataResolver.resolve(candidate);
        JobData jobData = jobRequirementResolver.resolve(job);

        RuleBasedMatchingEngine.DeterministicScore score =
                matchingEngine.calculate(candidateData, jobData);

        Set<String> candidateSkillSet = candidateData.getSkills() != null
                ? Set.copyOf(candidateData.getSkills()) : Set.of();
        Set<String> jobSkillSet = jobData.getRequiredSkills() != null
                ? Set.copyOf(jobData.getRequiredSkills()) : Set.of();

        List<String> matchedSkills = score.getMatchedSkills();
        List<String> missingSkills = score.getMissingSkills();
        List<String> partialSkills = new ArrayList<>();

        for (String candidateSkill : candidateSkillSet) {
            for (String jobSkill : jobSkillSet) {
                if (candidateSkill.contains(jobSkill) || jobSkill.contains(candidateSkill)) {
                    if (!matchedSkills.contains(candidateSkill) && !missingSkills.contains(jobSkill)) {
                        partialSkills.add(jobSkill);
                    }
                }
            }
        }

        List<String> prioritySuggestions = buildPrioritySuggestions(missingSkills, jobData);

        SkillGapResponse.SkillGapResponseBuilder builder = SkillGapResponse.builder()
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .jobRequiredSkills(jobData.getRequiredSkills() != null
                        ? jobData.getRequiredSkills().stream().sorted().toList() : List.of())
                .candidateSkills(candidateData.getSkills() != null
                        ? candidateData.getSkills().stream().sorted().toList() : List.of())
                .matchedSkills(matchedSkills.stream().sorted().toList())
                .missingSkills(missingSkills.stream().sorted().toList())
                .partiallyMatchedSkills(partialSkills.stream().sorted().toList())
                .prioritySuggestions(prioritySuggestions)
                .aiEnhanced(false);

        try {
            Optional<AIMatchingService.SkillGapResult> aiResult =
                    aiMatchingService.analyzeSkillGap(candidateData, jobData, candidate.getId());
            if (aiResult.isPresent()) {
                AIMatchingService.SkillGapResult ai = aiResult.get();
                if (ai.getExplanation() != null) {
                    builder.explanation(ai.getExplanation());
                }
                if (!ai.getPrioritySuggestions().isEmpty()) {
                    builder.prioritySuggestions(ai.getPrioritySuggestions());
                }
                builder.aiEnhanced(true);
            }
        } catch (Exception e) {
            // fallback to deterministic
        }

        if (builder.build().getExplanation() == null) {
            builder.explanation(buildDeterministicExplanation(matchedSkills, missingSkills));
        }

        return builder.build();
    }

    private List<String> buildPrioritySuggestions(List<String> missingSkills, JobData job) {
        List<String> suggestions = new ArrayList<>();
        for (String skill : missingSkills) {
            suggestions.add("Consider strengthening " + skill + " - it is required for this role");
        }
        if (missingSkills.size() > 3) {
            suggestions.add("Focus on the most relevant skills first");
        }
        return suggestions;
    }

    private String buildDeterministicExplanation(List<String> matched, List<String> missing) {
        if (missing.isEmpty()) {
            return "You have all the required skills for this position.";
        }
        return "You match " + matched.size() + " of " + (matched.size() + missing.size())
                + " required skills. Consider developing: " + String.join(", ", missing);
    }
}
