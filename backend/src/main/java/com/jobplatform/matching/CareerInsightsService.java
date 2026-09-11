package com.jobplatform.matching;

import com.jobplatform.matching.dto.CareerInsightsResponse;
import com.jobplatform.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CareerInsightsService {

    private final CandidateDataResolver candidateDataResolver;
    private final AIMatchingService aiMatchingService;

    public CareerInsightsService(
            CandidateDataResolver candidateDataResolver,
            AIMatchingService aiMatchingService) {
        this.candidateDataResolver = candidateDataResolver;
        this.aiMatchingService = aiMatchingService;
    }

    public CareerInsightsResponse getCareerInsights(User candidate) {
        CandidateData candidateData = candidateDataResolver.resolve(candidate);

        List<String> strengths = buildStrengths(candidateData);
        List<String> recommendedSkills = buildRecommendedSkills(candidateData);
        List<String> suggestedCategories = buildSuggestedCategories(candidateData);
        List<String> profileImprovements = buildProfileImprovements(candidateData);
        List<String> resumeImprovements = buildResumeImprovements(candidateData);
        List<String> careerSuggestions = buildCareerSuggestions(candidateData);

        boolean aiEnhanced = false;

        try {
            Optional<AIMatchingService.CareerAdviceResult> aiResult =
                    aiMatchingService.generateCareerAdvice(candidateData, candidate.getId());
            if (aiResult.isPresent()) {
                AIMatchingService.CareerAdviceResult ai = aiResult.get();
                if (!ai.getStrengths().isEmpty()) {
                    strengths = ai.getStrengths();
                }
                if (!ai.getRecommendedSkills().isEmpty()) {
                    recommendedSkills = ai.getRecommendedSkills();
                }
                if (!ai.getSuggestedJobCategories().isEmpty()) {
                    suggestedCategories = ai.getSuggestedJobCategories();
                }
                if (!ai.getProfileImprovements().isEmpty()) {
                    profileImprovements = ai.getProfileImprovements();
                }
                if (!ai.getGeneralCareerSuggestions().isEmpty()) {
                    careerSuggestions = ai.getGeneralCareerSuggestions();
                }
                aiEnhanced = true;
            }
        } catch (Exception e) {
            // fallback to deterministic
        }

        return CareerInsightsResponse.builder()
                .strengths(strengths)
                .recommendedSkills(recommendedSkills)
                .suggestedJobCategories(suggestedCategories)
                .profileImprovements(profileImprovements)
                .resumeImprovements(resumeImprovements)
                .generalCareerSuggestions(careerSuggestions)
                .aiEnhanced(aiEnhanced)
                .build();
    }

    private List<String> buildStrengths(CandidateData candidate) {
        List<String> strengths = new ArrayList<>();
        if (candidate.getSkills() != null && !candidate.getSkills().isEmpty()) {
            strengths.add("Strong skill set with " + candidate.getSkills().size() + " skills listed");
        }
        if (candidate.getYearsOfExperience() != null && candidate.getYearsOfExperience() > 0) {
            strengths.add(candidate.getYearsOfExperience() + " years of professional experience");
        }
        if (candidate.getProfessionalSummary() != null && !candidate.getProfessionalSummary().isBlank()) {
            strengths.add("Clear professional summary provided");
        }
        if (candidate.isResumeDataAvailable()) {
            strengths.add("Resume data available for analysis");
        }
        if (strengths.isEmpty()) {
            strengths.add("Profile created and ready for enhancement");
        }
        return strengths;
    }

    private List<String> buildRecommendedSkills(CandidateData candidate) {
        List<String> recommended = new ArrayList<>();
        if (candidate.getSkills() == null || candidate.getSkills().isEmpty()) {
            recommended.add("Add skills to your profile to improve matching");
            return recommended;
        }
        int size = candidate.getSkills().size();
        if (size < 5) {
            recommended.add("Consider adding more skills to your profile");
        }
        if (!candidate.getSkills().stream().anyMatch(s -> s.contains("communication") || s.contains("leadership"))) {
            recommended.add("Soft skills like communication and leadership are valued");
        }
        return recommended;
    }

    private List<String> buildSuggestedCategories(CandidateData candidate) {
        List<String> categories = new ArrayList<>();
        if (candidate.getSkills() != null) {
            boolean hasBackend = candidate.getSkills().stream()
                    .anyMatch(s -> s.contains("java") || s.contains("python") || s.contains("node"));
            boolean hasFrontend = candidate.getSkills().stream()
                    .anyMatch(s -> s.contains("react") || s.contains("angular") || s.contains("vue"));
            boolean hasData = candidate.getSkills().stream()
                    .anyMatch(s -> s.contains("sql") || s.contains("python") || s.contains("data"));

            if (hasBackend) categories.add("Backend Development");
            if (hasFrontend) categories.add("Frontend Development");
            if (hasData) categories.add("Data Engineering");
            if (hasBackend && hasFrontend) categories.add("Full-Stack Development");
        }
        if (categories.isEmpty()) {
            categories.add("Add skills to get personalized category suggestions");
        }
        return categories;
    }

    private List<String> buildProfileImprovements(CandidateData candidate) {
        List<String> improvements = new ArrayList<>();
        if (candidate.getHeadline() == null || candidate.getHeadline().isBlank()) {
            improvements.add("Add a professional headline to your profile");
        }
        if (candidate.getBio() == null || candidate.getBio().isBlank()) {
            improvements.add("Add a brief bio to describe your professional background");
        }
        if (!candidate.isResumeDataAvailable()) {
            improvements.add("Upload and parse a resume for better matching");
        }
        if (candidate.getSkills() == null || candidate.getSkills().size() < 3) {
            improvements.add("Add at least 3-5 skills to improve job matching");
        }
        if (improvements.isEmpty()) {
            improvements.add("Your profile is well-optimized");
        }
        return improvements;
    }

    private List<String> buildResumeImprovements(CandidateData candidate) {
        List<String> improvements = new ArrayList<>();
        if (!candidate.isResumeDataAvailable()) {
            improvements.add("Upload a resume to unlock detailed analysis");
            return improvements;
        }
        improvements.add("Ensure your resume includes quantifiable achievements");
        improvements.add("Use industry-relevant keywords in your resume");
        return improvements;
    }

    private List<String> buildCareerSuggestions(CandidateData candidate) {
        List<String> suggestions = new ArrayList<>();
        if (candidate.getYearsOfExperience() != null) {
            if (candidate.getYearsOfExperience() < 3) {
                suggestions.add("Focus on building foundational skills and gaining experience");
            } else if (candidate.getYearsOfExperience() < 7) {
                suggestions.add("Consider specializing in a technical niche");
            } else {
                suggestions.add("Consider mentoring or technical leadership roles");
            }
        }
        suggestions.add("Keep your skills and profile up to date");
        suggestions.add("Regularly review job market trends in your field");
        return suggestions;
    }
}
