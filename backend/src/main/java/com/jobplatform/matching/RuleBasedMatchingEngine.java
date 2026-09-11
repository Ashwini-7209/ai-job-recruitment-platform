package com.jobplatform.matching;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class RuleBasedMatchingEngine {

    static final double SKILL_WEIGHT = 0.50;
    static final double EXPERIENCE_WEIGHT = 0.25;
    static final double PROFILE_WEIGHT = 0.15;
    static final double AI_ENHANCEMENT_WEIGHT = 0.15;

    public DeterministicScore calculate(CandidateData candidate, JobData job) {
        Set<String> candidateSkills = candidate.getSkills() != null
                ? Set.copyOf(candidate.getSkills()) : Set.of();
        Set<String> jobSkills = job.getRequiredSkills() != null
                ? Set.copyOf(job.getRequiredSkills()) : Set.of();

        Set<String> matchedSkills = SkillNormalizer.findMatched(candidateSkills, jobSkills);
        Set<String> missingSkills = SkillNormalizer.findMissing(candidateSkills, jobSkills);

        double skillScore = calculateSkillScore(candidateSkills, jobSkills, matchedSkills);
        double experienceScore = calculateExperienceScore(candidate.getYearsOfExperience(),
                job.getExperienceMin(), job.getExperienceMax());
        double profileScore = calculateProfileScore(candidate, job);

        double deterministicScore = skillScore * SKILL_WEIGHT
                + experienceScore * EXPERIENCE_WEIGHT
                + profileScore * PROFILE_WEIGHT;

        deterministicScore = Math.max(0, Math.min(100, Math.round(deterministicScore)));

        List<String> matchingReasons = buildMatchingReasons(matchedSkills, jobSkills,
                candidate.getYearsOfExperience(), job.getExperienceMin(), job.getExperienceMax(),
                candidate, job);

        List<String> potentialGaps = buildPotentialGaps(missingSkills,
                candidate.getYearsOfExperience(), job.getExperienceMin());

        return DeterministicScore.builder()
                .overallScore((int) deterministicScore)
                .skillScore((int) Math.round(skillScore))
                .experienceScore((int) Math.round(experienceScore))
                .profileScore((int) Math.round(profileScore))
                .matchedSkills(matchedSkills.stream().sorted().toList())
                .missingSkills(missingSkills.stream().sorted().toList())
                .matchingReasons(matchingReasons)
                .potentialGaps(potentialGaps)
                .build();
    }

    private double calculateSkillScore(Set<String> candidateSkills, Set<String> jobSkills,
                                        Set<String> matchedSkills) {
        if (jobSkills.isEmpty()) {
            return candidateSkills.isEmpty() ? 50.0 : 70.0;
        }
        if (candidateSkills.isEmpty()) {
            return 0.0;
        }
        return (double) matchedSkills.size() / jobSkills.size() * 100.0;
    }

    private double calculateExperienceScore(Integer candidateYoe, Integer jobMin, Integer jobMax) {
        if (jobMin == null && jobMax == null) {
            return candidateYoe != null ? 70.0 : 50.0;
        }

        int actualYoe = candidateYoe != null ? candidateYoe : 0;

        if (jobMin != null && jobMax != null) {
            if (actualYoe >= jobMin && actualYoe <= jobMax) {
                return 100.0;
            }
            if (actualYoe < jobMin) {
                int deficit = jobMin - actualYoe;
                if (deficit <= 1) return 70.0;
                if (deficit <= 2) return 50.0;
                return 25.0;
            }
            double overRatio = (double) (actualYoe - jobMax) / jobMax;
            if (overRatio <= 0.5) return 90.0;
            if (overRatio <= 1.0) return 75.0;
            return 60.0;
        }

        if (jobMin != null) {
            if (actualYoe >= jobMin) return 100.0;
            int deficit = jobMin - actualYoe;
            if (deficit <= 1) return 70.0;
            if (deficit <= 2) return 50.0;
            return 25.0;
        }

        if (jobMax != null) {
            if (actualYoe <= jobMax) return 90.0;
            return 60.0;
        }

        return 50.0;
    }

    private double calculateProfileScore(CandidateData candidate, JobData job) {
        double score = 0.0;
        int factors = 0;

        if (candidate.getHeadline() != null && !candidate.getHeadline().isBlank()) {
            score += 30.0;
            factors++;
        }
        if (candidate.getBio() != null && !candidate.getBio().isBlank()) {
            score += 25.0;
            factors++;
        }
        if (candidate.getProfessionalSummary() != null && !candidate.getProfessionalSummary().isBlank()) {
            score += 25.0;
            factors++;
        }
        if (candidate.isResumeDataAvailable()) {
            score += 20.0;
            factors++;
        }

        if (candidate.getLocation() != null && job.getLocation() != null
                && candidate.getLocation().equalsIgnoreCase(job.getLocation())) {
            score += 10.0;
            factors++;
        }

        if (factors == 0) {
            return 10.0;
        }

        return Math.min(100.0, score);
    }

    private List<String> buildMatchingReasons(Set<String> matchedSkills, Set<String> jobSkills,
                                               Integer candidateYoe, Integer jobMin, Integer jobMax,
                                               CandidateData candidate, JobData job) {
        List<String> reasons = new ArrayList<>();

        if (!jobSkills.isEmpty() && !matchedSkills.isEmpty()) {
            reasons.add(matchedSkills.size() + " of " + jobSkills.size()
                    + " required skills matched");
        } else if (jobSkills.isEmpty()) {
            reasons.add("No specific skills required for this role");
        }

        if (candidateYoe != null) {
            if (jobMin != null && jobMax != null) {
                if (candidateYoe >= jobMin && candidateYoe <= jobMax) {
                    reasons.add("Experience of " + candidateYoe + " years fits the required range ("
                            + jobMin + "-" + jobMax + " years)");
                } else if (candidateYoe >= jobMin) {
                    reasons.add("Candidate has " + candidateYoe + " years of experience");
                }
            } else if (jobMin != null && candidateYoe >= jobMin) {
                reasons.add("Meets minimum experience requirement of " + jobMin + " years");
            } else {
                reasons.add("Candidate has " + candidateYoe + " years of experience");
            }
        }

        if (candidate.isResumeDataAvailable()) {
            reasons.add("Resume data available for matching analysis");
        } else if (candidate.isProfileDataAvailable()) {
            reasons.add("Profile data used for matching");
        } else {
            reasons.add("Limited profile data available");
        }

        return reasons;
    }

    private List<String> buildPotentialGaps(Set<String> missingSkills,
                                             Integer candidateYoe, Integer jobMin) {
        List<String> gaps = new ArrayList<>();

        if (!missingSkills.isEmpty()) {
            gaps.add("Missing skills: " + String.join(", ", missingSkills.stream().sorted().toList()));
        }

        if (jobMin != null && (candidateYoe == null || candidateYoe < jobMin)) {
            int required = jobMin;
            int actual = candidateYoe != null ? candidateYoe : 0;
            gaps.add("Requires " + required + "+ years, candidate has approximately " + actual + " years");
        }

        return gaps;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DeterministicScore {
        private int overallScore;
        private int skillScore;
        private int experienceScore;
        private int profileScore;
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private List<String> matchingReasons;
        private List<String> potentialGaps;
    }
}
