package com.jobplatform.matching;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobplatform.ai.ChatAIProvider;
import com.jobplatform.ai.AIUsageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class AIMatchingService {

    private static final Logger log = LoggerFactory.getLogger(AIMatchingService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ChatAIProvider chatAIProvider;
    private final AIUsageService usageService;

    public AIMatchingService(ChatAIProvider chatAIProvider, AIUsageService usageService) {
        this.chatAIProvider = chatAIProvider;
        this.usageService = usageService;
    }

    public Optional<AISemanticResult> evaluateMatch(CandidateData candidate, JobData job, Long userId) {
        if (!chatAIProvider.isAvailable()) {
            return Optional.empty();
        }

        if (userId != null && !usageService.canMakeRequest(userId)) {
            log.info("AI request limit exceeded for user {}, falling back to deterministic", userId);
            return Optional.empty();
        }

        String prompt = buildMatchPrompt(candidate, job);

        Optional<String> responseOpt = chatAIProvider.chat(
                "You are an expert job matching analyst.", prompt, 2000);

        if (responseOpt.isEmpty()) {
            return Optional.empty();
        }

        if (userId != null) {
            usageService.recordRequest(userId);
        }

        return parseMatchResponse(responseOpt.get());
    }

    public String buildMatchExplanation(CandidateData candidate, JobData job) {
        if (!chatAIProvider.isAvailable()) {
            return buildDeterministicExplanation(candidate, job);
        }

        String prompt = buildExplanationPrompt(candidate, job);
        Optional<String> responseOpt = chatAIProvider.chat(
                "You are an expert career advisor. Provide a concise, factual explanation.",
                prompt, 1000);

        return responseOpt.orElseGet(() -> buildDeterministicExplanation(candidate, job));
    }

    public Optional<SkillGapResult> analyzeSkillGap(CandidateData candidate, JobData job, Long userId) {
        if (!chatAIProvider.isAvailable()) {
            return Optional.empty();
        }

        if (userId != null && !usageService.canMakeRequest(userId)) {
            return Optional.empty();
        }

        String prompt = buildSkillGapPrompt(candidate, job);
        Optional<String> responseOpt = chatAIProvider.chat(
                "You are an expert skill gap analyst.", prompt, 1500);

        if (responseOpt.isEmpty()) {
            return Optional.empty();
        }

        if (userId != null) {
            usageService.recordRequest(userId);
        }

        return parseSkillGapResponse(responseOpt.get());
    }

    public Optional<CareerAdviceResult> generateCareerAdvice(CandidateData candidate, Long userId) {
        if (!chatAIProvider.isAvailable()) {
            return Optional.empty();
        }

        if (userId != null && !usageService.canPerformAnalysis(userId)) {
            return Optional.empty();
        }

        String prompt = buildCareerAdvicePrompt(candidate);
        Optional<String> responseOpt = chatAIProvider.chat(
                "You are an expert career advisor. Provide actionable, factual advice.",
                prompt, 2000);

        if (responseOpt.isEmpty()) {
            return Optional.empty();
        }

        if (userId != null) {
            usageService.recordAnalysis(userId);
        }

        return parseCareerAdviceResponse(responseOpt.get());
    }

    public Optional<ResumeImprovementResult> analyzeResumeImprovements(
            String resumeText, String skills, Long userId) {
        if (!chatAIProvider.isAvailable()) {
            return Optional.empty();
        }

        if (userId != null && !usageService.canPerformAnalysis(userId)) {
            return Optional.empty();
        }

        String prompt = buildResumeImprovementPrompt(resumeText, skills);
        Optional<String> responseOpt = chatAIProvider.chat(
                "You are an expert resume analyst. Provide actionable, factual suggestions.",
                prompt, 2000);

        if (responseOpt.isEmpty()) {
            return Optional.empty();
        }

        if (userId != null) {
            usageService.recordAnalysis(userId);
        }

        return parseResumeImprovementResponse(responseOpt.get());
    }

    private String buildDeterministicExplanation(CandidateData candidate, JobData job) {
        StringBuilder sb = new StringBuilder();
        sb.append("Based on available data: ");

        if (candidate.getSkills() != null && !candidate.getSkills().isEmpty()) {
            Set<String> matched = SkillNormalizer.findMatched(
                    Set.copyOf(candidate.getSkills()),
                    job.getRequiredSkills() != null ? Set.copyOf(job.getRequiredSkills()) : Set.of());
            if (!matched.isEmpty()) {
                sb.append("You have ").append(matched.size()).append(" matching skill(s). ");
            }
        }

        if (candidate.getYearsOfExperience() != null) {
            sb.append("Your ").append(candidate.getYearsOfExperience()).append(" years of experience ");
            if (job.getExperienceMin() != null) {
                sb.append(candidate.getYearsOfExperience() >= job.getExperienceMin()
                        ? "meets the minimum requirement. "
                        : "is below the minimum requirement of ").append(job.getExperienceMin()).append(" years. ");
            } else {
                sb.append("is relevant to this role. ");
            }
        }

        Set<String> missing = candidate.getSkills() != null
                ? SkillNormalizer.findMissing(Set.copyOf(candidate.getSkills()),
                    job.getRequiredSkills() != null ? Set.copyOf(job.getRequiredSkills()) : Set.of())
                : Set.of();
        if (!missing.isEmpty()) {
            sb.append("Consider strengthening skills in: ").append(String.join(", ", missing)).append(". ");
        }

        return sb.toString();
    }

    // --- Prompt Builders ---

    private String buildMatchPrompt(CandidateData candidate, JobData job) {
        return """
                Evaluate how well a candidate matches a job role.

                CANDIDATE:
                Headline: %s
                Skills: %s
                Experience: %s years
                Summary: %s

                JOB:
                Title: %s
                Required Skills: %s
                Experience Range: %s-%s years

                Return ONLY valid JSON:
                {
                  "relevanceScore": 0-100,
                  "matchingReasons": ["reason1"],
                  "potentialGaps": ["gap1"],
                  "evidence": ["evidence1"]
                }""".formatted(
                safe(candidate.getHeadline()), safeSkills(candidate.getSkills()),
                candidate.getYearsOfExperience() != null ? candidate.getYearsOfExperience() : "unknown",
                safe(candidate.getProfessionalSummary()),
                job.getTitle(), safeSkills(job.getRequiredSkills()),
                job.getExperienceMin() != null ? job.getExperienceMin() : "n/a",
                job.getExperienceMax() != null ? job.getExperienceMax() : "n/a");
    }

    private String buildExplanationPrompt(CandidateData candidate, JobData job) {
        return """
                Write a 2-3 sentence explanation of why this job matches this candidate.
                Base ONLY on the data provided. Do not invent information.

                CANDIDATE: Skills=%s, Experience=%s years, Headline=%s
                JOB: Title=%s, Required Skills=%s, Experience=%s-%s years

                Return only the explanation text, no JSON.""".formatted(
                safeSkills(candidate.getSkills()),
                candidate.getYearsOfExperience() != null ? candidate.getYearsOfExperience() : "unknown",
                safe(candidate.getHeadline()),
                job.getTitle(), safeSkills(job.getRequiredSkills()),
                job.getExperienceMin() != null ? job.getExperienceMin() : "n/a",
                job.getExperienceMax() != null ? job.getExperienceMax() : "n/a");
    }

    private String buildSkillGapPrompt(CandidateData candidate, JobData job) {
        return """
                Analyze the skill gap between a candidate and a job.

                CANDIDATE SKILLS: %s
                JOB REQUIRED SKILLS: %s

                Return ONLY valid JSON:
                {
                  "matchedSkills": ["skill1"],
                  "missingSkills": ["skill1"],
                  "partiallyMatchedSkills": ["skill1"],
                  "prioritySuggestions": ["suggestion1"],
                  "explanation": "brief explanation"
                }""".formatted(safeSkills(candidate.getSkills()), safeSkills(job.getRequiredSkills()));
    }

    private String buildCareerAdvicePrompt(CandidateData candidate) {
        return """
                Based on this candidate profile, provide career advice.

                SKILLS: %s
                EXPERIENCE: %s years
                HEADLINE: %s
                SUMMARY: %s

                Return ONLY valid JSON:
                {
                  "strengths": ["strength1"],
                  "recommendedSkills": ["skill1"],
                  "suggestedJobCategories": ["category1"],
                  "profileImprovements": ["improvement1"],
                  "generalCareerSuggestions": ["suggestion1"]
                }""".formatted(
                safeSkills(candidate.getSkills()),
                candidate.getYearsOfExperience() != null ? candidate.getYearsOfExperience() : "unknown",
                safe(candidate.getHeadline()),
                safe(candidate.getProfessionalSummary()));
    }

    private String buildResumeImprovementPrompt(String resumeText, String skills) {
        String truncated = resumeText.length() > 3000
                ? resumeText.substring(0, 3000) + "..."
                : resumeText;
        return """
                Analyze this resume and suggest improvements.

                RESUME TEXT:
                ---
                %s
                ---

                TARGET SKILLS: %s

                Return ONLY valid JSON:
                {
                  "completeness": 0-100,
                  "strengths": ["strength1"],
                  "improvements": ["improvement1"],
                  "missingSections": ["section1"],
                  "keywordSuggestions": ["keyword1"]
                }""".formatted(truncated, skills != null ? skills : "none specified");
    }

    // --- Response Parsers ---

    private Optional<AISemanticResult> parseMatchResponse(String response) {
        try {
            String content = stripMarkdownFences(response);
            JsonNode data = objectMapper.readTree(content);

            int relevanceScore = Math.max(0, Math.min(100,
                    data.path("relevanceScore").asInt(0)));

            AISemanticResult result = AISemanticResult.builder()
                    .relevanceScore(relevanceScore)
                    .matchingReasons(parseStringList(data.path("matchingReasons")))
                    .potentialGaps(parseStringList(data.path("potentialGaps")))
                    .evidence(parseStringList(data.path("evidence")))
                    .build();

            return Optional.of(result);
        } catch (Exception e) {
            log.warn("Failed to parse AI match response: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<SkillGapResult> parseSkillGapResponse(String response) {
        try {
            String content = stripMarkdownFences(response);
            JsonNode data = objectMapper.readTree(content);

            SkillGapResult result = SkillGapResult.builder()
                    .matchedSkills(parseStringList(data.path("matchedSkills")))
                    .missingSkills(parseStringList(data.path("missingSkills")))
                    .partiallyMatchedSkills(parseStringList(data.path("partiallyMatchedSkills")))
                    .prioritySuggestions(parseStringList(data.path("prioritySuggestions")))
                    .explanation(getNullOrString(data, "explanation"))
                    .build();

            return Optional.of(result);
        } catch (Exception e) {
            log.warn("Failed to parse AI skill gap response: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<CareerAdviceResult> parseCareerAdviceResponse(String response) {
        try {
            String content = stripMarkdownFences(response);
            JsonNode data = objectMapper.readTree(content);

            CareerAdviceResult result = CareerAdviceResult.builder()
                    .strengths(parseStringList(data.path("strengths")))
                    .recommendedSkills(parseStringList(data.path("recommendedSkills")))
                    .suggestedJobCategories(parseStringList(data.path("suggestedJobCategories")))
                    .profileImprovements(parseStringList(data.path("profileImprovements")))
                    .generalCareerSuggestions(parseStringList(data.path("generalCareerSuggestions")))
                    .build();

            return Optional.of(result);
        } catch (Exception e) {
            log.warn("Failed to parse AI career advice response: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<ResumeImprovementResult> parseResumeImprovementResponse(String response) {
        try {
            String content = stripMarkdownFences(response);
            JsonNode data = objectMapper.readTree(content);

            int completeness = Math.max(0, Math.min(100,
                    data.path("completeness").asInt(0)));

            ResumeImprovementResult result = ResumeImprovementResult.builder()
                    .completeness(completeness)
                    .strengths(parseStringList(data.path("strengths")))
                    .improvements(parseStringList(data.path("improvements")))
                    .missingSections(parseStringList(data.path("missingSections")))
                    .keywordSuggestions(parseStringList(data.path("keywordSuggestions")))
                    .build();

            return Optional.of(result);
        } catch (Exception e) {
            log.warn("Failed to parse AI resume improvement response: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // --- Utilities ---

    private String stripMarkdownFences(String text) {
        String stripped = text.strip();
        if (stripped.startsWith("```json")) {
            stripped = stripped.substring(7);
        }
        if (stripped.endsWith("```")) {
            stripped = stripped.substring(0, stripped.length() - 3);
        }
        return stripped.strip();
    }

    private String safe(String s) {
        return s != null ? s : "Not provided";
    }

    private String safeSkills(List<String> skills) {
        return skills != null && !skills.isEmpty() ? String.join(", ", skills) : "None listed";
    }

    private List<String> parseStringList(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return list;
        }
        for (JsonNode item : node) {
            if (!item.isNull() && !item.isMissingNode()) {
                String val = item.asText("").strip();
                if (!val.isEmpty()) {
                    list.add(val);
                }
            }
        }
        return list;
    }

    private String getNullOrString(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNull() || value.isMissingNode()) {
            return null;
        }
        String text = value.asText("").strip();
        return text.isEmpty() ? null : text;
    }

    // --- Result DTOs ---

    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class AISemanticResult {
        private int relevanceScore;
        private List<String> matchingReasons;
        private List<String> potentialGaps;
        private List<String> evidence;
    }

    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class SkillGapResult {
        private List<String> matchedSkills;
        private List<String> missingSkills;
        private List<String> partiallyMatchedSkills;
        private List<String> prioritySuggestions;
        private String explanation;
    }

    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class CareerAdviceResult {
        private List<String> strengths;
        private List<String> recommendedSkills;
        private List<String> suggestedJobCategories;
        private List<String> profileImprovements;
        private List<String> generalCareerSuggestions;
    }

    @lombok.Data @lombok.Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ResumeImprovementResult {
        private int completeness;
        private List<String> strengths;
        private List<String> improvements;
        private List<String> missingSections;
        private List<String> keywordSuggestions;
    }
}
