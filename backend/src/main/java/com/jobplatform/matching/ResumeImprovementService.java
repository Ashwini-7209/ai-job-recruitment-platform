package com.jobplatform.matching;

import com.jobplatform.matching.dto.ResumeImprovementResponse;
import com.jobplatform.resume.Resume;
import com.jobplatform.resume.ResumeProfileData;
import com.jobplatform.resume.ResumeRepository;
import com.jobplatform.resume.enums.ResumeParsingStatus;
import com.jobplatform.resume.parsing.ResumeProfileDataRepository;
import com.jobplatform.resume.storage.FileStorageService;
import com.jobplatform.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeImprovementService {

    private static final Logger log = LoggerFactory.getLogger(ResumeImprovementService.class);

    private final ResumeRepository resumeRepository;
    private final ResumeProfileDataRepository resumeProfileDataRepository;
    private final FileStorageService fileStorageService;
    private final AIMatchingService aiMatchingService;

    public ResumeImprovementService(
            ResumeRepository resumeRepository,
            ResumeProfileDataRepository resumeProfileDataRepository,
            FileStorageService fileStorageService,
            AIMatchingService aiMatchingService) {
        this.resumeRepository = resumeRepository;
        this.resumeProfileDataRepository = resumeProfileDataRepository;
        this.fileStorageService = fileStorageService;
        this.aiMatchingService = aiMatchingService;
    }

    @Transactional(readOnly = true)
    public ResumeImprovementResponse analyzeResume(User candidate, Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .filter(r -> r.getCandidate().getId().equals(candidate.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (resume.getParsingStatus() != ResumeParsingStatus.COMPLETED) {
            return buildUnparsedResponse(resume);
        }

        Optional<ResumeProfileData> profileDataOpt =
                resumeProfileDataRepository.findByResume(resume);

        if (profileDataOpt.isEmpty()) {
            return buildUnparsedResponse(resume);
        }

        ResumeProfileData profileData = profileDataOpt.get();
        String resumeText = extractResumeText(profileData);
        String skills = profileData.getSkills();

        int completeness = calculateCompleteness(profileData);
        List<String> strengths = findStrengths(profileData);
        List<String> improvements = findImprovements(profileData);
        List<String> missingSections = findMissingSections(profileData);
        List<String> keywordSuggestions = findKeywordSuggestions(profileData);

        boolean aiEnhanced = false;

        try {
            Optional<AIMatchingService.ResumeImprovementResult> aiResult =
                    aiMatchingService.analyzeResumeImprovements(resumeText, skills, candidate.getId());
            if (aiResult.isPresent()) {
                AIMatchingService.ResumeImprovementResult ai = aiResult.get();
                completeness = ai.getCompleteness();
                if (!ai.getStrengths().isEmpty()) {
                    strengths = ai.getStrengths();
                }
                if (!ai.getImprovements().isEmpty()) {
                    improvements = ai.getImprovements();
                }
                if (!ai.getMissingSections().isEmpty()) {
                    missingSections = ai.getMissingSections();
                }
                if (!ai.getKeywordSuggestions().isEmpty()) {
                    keywordSuggestions = ai.getKeywordSuggestions();
                }
                aiEnhanced = true;
            }
        } catch (Exception e) {
            log.warn("AI resume analysis failed, using deterministic: {}", e.getMessage());
        }

        return ResumeImprovementResponse.builder()
                .resumeId(resumeId)
                .completeness(completeness)
                .strengths(strengths)
                .improvements(improvements)
                .missingSections(missingSections)
                .keywordSuggestions(keywordSuggestions)
                .aiEnhanced(aiEnhanced)
                .build();
    }

    private int calculateCompleteness(ResumeProfileData data) {
        int score = 0;
        int total = 10;

        if (data.getFullName() != null && !data.getFullName().isBlank()) score++;
        if (data.getEmail() != null && !data.getEmail().isBlank()) score++;
        if (data.getPhone() != null && !data.getPhone().isBlank()) score++;
        if (data.getProfessionalSummary() != null && !data.getProfessionalSummary().isBlank()) score++;
        if (data.getHeadline() != null && !data.getHeadline().isBlank()) score++;
        if (data.getSkills() != null && !data.getSkills().isBlank()) score++;
        if (data.getExperiences() != null && !data.getExperiences().isEmpty()) score++;
        if (data.getEducation() != null && !data.getEducation().isEmpty()) score++;
        if (data.getLanguages() != null && !data.getLanguages().isBlank()) score++;
        if (data.getCertifications() != null && !data.getCertifications().isEmpty()) score++;

        return (int) Math.round((double) score / total * 100);
    }

    private List<String> findStrengths(ResumeProfileData data) {
        List<String> strengths = new ArrayList<>();
        if (data.getSkills() != null && !data.getSkills().isBlank()) {
            int skillCount = data.getSkills().split(",").length;
            strengths.add(skillCount + " skills identified in your resume");
        }
        if (data.getExperiences() != null && !data.getExperiences().isEmpty()) {
            strengths.add(data.getExperiences().size() + " work experience(s) documented");
        }
        if (data.getProfessionalSummary() != null && !data.getProfessionalSummary().isBlank()) {
            strengths.add("Professional summary is present");
        }
        if (data.getProjects() != null && !data.getProjects().isEmpty()) {
            strengths.add(data.getProjects().size() + " project(s) documented");
        }
        if (strengths.isEmpty()) {
            strengths.add("Resume has been parsed successfully");
        }
        return strengths;
    }

    private List<String> findImprovements(ResumeProfileData data) {
        List<String> improvements = new ArrayList<>();
        if (data.getProfessionalSummary() == null || data.getProfessionalSummary().isBlank()) {
            improvements.add("Add a professional summary to introduce yourself");
        }
        if (data.getSkills() == null || data.getSkills().isBlank()) {
            improvements.add("List your technical and soft skills");
        }
        if (data.getExperiences() == null || data.getExperiences().isEmpty()) {
            improvements.add("Add work experience to demonstrate your background");
        } else {
            for (var exp : data.getExperiences()) {
                if (exp.getDescription() == null || exp.getDescription().isBlank()) {
                    improvements.add("Add descriptions to your work experience entries");
                    break;
                }
            }
        }
        if (data.getEducation() == null || data.getEducation().isEmpty()) {
            improvements.add("Consider adding education details");
        }
        return improvements;
    }

    private List<String> findMissingSections(ResumeProfileData data) {
        List<String> missing = new ArrayList<>();
        if (data.getPhone() == null || data.getPhone().isBlank()) {
            missing.add("Contact phone number");
        }
        if (data.getHeadline() == null || data.getHeadline().isBlank()) {
            missing.add("Professional headline");
        }
        if (data.getLanguages() == null || data.getLanguages().isBlank()) {
            missing.add("Languages section");
        }
        if (data.getCertifications() == null || data.getCertifications().isEmpty()) {
            missing.add("Certifications (optional but recommended)");
        }
        return missing;
    }

    private List<String> findKeywordSuggestions(ResumeProfileData data) {
        List<String> keywords = new ArrayList<>();
        if (data.getSkills() != null && !data.getSkills().isBlank()) {
            String[] skills = data.getSkills().split(",");
            if (skills.length < 5) {
                keywords.add("Add more industry-relevant keywords");
            }
        }
        keywords.add("Include quantifiable achievements (e.g., 'increased revenue by 20%')");
        keywords.add("Use action verbs to describe your experience");
        return keywords;
    }

    private String extractResumeText(ResumeProfileData data) {
        StringBuilder sb = new StringBuilder();
        if (data.getProfessionalSummary() != null) {
            sb.append(data.getProfessionalSummary()).append("\n");
        }
        if (data.getSkills() != null) {
            sb.append("Skills: ").append(data.getSkills()).append("\n");
        }
        if (data.getExperiences() != null) {
            for (var exp : data.getExperiences()) {
                if (exp.getDescription() != null) {
                    sb.append(exp.getDescription()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    private ResumeImprovementResponse buildUnparsedResponse(Resume resume) {
        return ResumeImprovementResponse.builder()
                .resumeId(resume.getId())
                .completeness(0)
                .strengths(List.of())
                .improvements(List.of("Please parse the resume first"))
                .missingSections(List.of("Resume needs to be parsed"))
                .keywordSuggestions(List.of())
                .aiEnhanced(false)
                .build();
    }
}
