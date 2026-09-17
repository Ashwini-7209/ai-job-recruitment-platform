package com.jobplatform.candidate;

import com.jobplatform.candidate.dto.CandidateProfileResponse;
import com.jobplatform.candidate.dto.CandidateProfileUpdateRequest;
import com.jobplatform.candidate.dto.ProfileCompletionResponse;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.resume.ResumeRepository;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CandidateProfileService {

    private static final Logger log = LoggerFactory.getLogger(CandidateProfileService.class);

    private final CandidateProfileRepository candidateProfileRepository;
    private final ResumeRepository resumeRepository;

    public CandidateProfileService(CandidateProfileRepository candidateProfileRepository,
                                   ResumeRepository resumeRepository) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.resumeRepository = resumeRepository;
    }

    @Transactional(readOnly = true)
    public CandidateProfile getProfileByUserId(Long userId) {
        return candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile", "userId", userId));
    }

    @Transactional(readOnly = true)
    public CandidateProfileResponse getProfileResponse(Long userId) {
        CandidateProfile profile = getProfileByUserId(userId);
        return mapToResponse(profile);
    }

    @Transactional
    public CandidateProfile createProfile(User user) {
        if (user.getRole() != UserRole.CANDIDATE) {
            throw new IllegalArgumentException("Only CANDIDATE users can have a candidate profile");
        }
        CandidateProfile profile = CandidateProfile.builder()
                .user(user)
                .build();
        CandidateProfile saved = candidateProfileRepository.save(profile);
        log.info("Created candidate profile for user: {}", user.getEmail());
        return saved;
    }

    @Transactional
    public CandidateProfileResponse updateProfile(Long userId, CandidateProfileUpdateRequest request) {
        CandidateProfile profile = getProfileByUserId(userId);
        applyUpdate(profile, request);
        CandidateProfile saved = candidateProfileRepository.save(profile);
        log.info("Updated candidate profile for userId: {}", userId);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProfileCompletionResponse getProfileCompletion(User user) {
        CandidateProfile profile = getProfileByUserId(user.getId());

        List<ProfileCompletionResponse.SectionCompletion> sections = new ArrayList<>();
        int totalWeight = 0;
        int earnedWeight = 0;

        // Personal Information (weight: 20)
        List<String> personalMissing = new ArrayList<>();
        if (profile.getPhone() == null || profile.getPhone().isBlank()) personalMissing.add("phone");
        if (profile.getLocation() == null || profile.getLocation().isBlank()) personalMissing.add("location");
        int personalWeight = 20;
        boolean personalComplete = personalMissing.isEmpty();
        int personalEarned = personalComplete ? personalWeight : (int)(personalWeight * (3 - personalMissing.size()) / 3.0);
        totalWeight += personalWeight;
        earnedWeight += personalEarned;
        sections.add(ProfileCompletionResponse.SectionCompletion.builder()
                .section("personal").label("Personal Information")
                .completed(personalComplete).weight(personalWeight)
                .missingFields(personalMissing).build());

        // Professional Summary (weight: 15)
        List<String> summaryMissing = new ArrayList<>();
        if (profile.getHeadline() == null || profile.getHeadline().isBlank()) summaryMissing.add("headline");
        if (profile.getBio() == null || profile.getBio().isBlank()) summaryMissing.add("bio");
        if (profile.getCurrentJobTitle() == null || profile.getCurrentJobTitle().isBlank()) summaryMissing.add("currentJobTitle");
        int summaryWeight = 15;
        boolean summaryComplete = summaryMissing.isEmpty();
        int summaryEarned = summaryComplete ? summaryWeight : (int)(summaryWeight * (3 - summaryMissing.size()) / 3.0);
        totalWeight += summaryWeight;
        earnedWeight += summaryEarned;
        sections.add(ProfileCompletionResponse.SectionCompletion.builder()
                .section("summary").label("Professional Summary")
                .completed(summaryComplete).weight(summaryWeight)
                .missingFields(summaryMissing).build());

        // Skills (weight: 15)
        List<String> skillsMissing = new ArrayList<>();
        if (profile.getSkillsSummary() == null || profile.getSkillsSummary().isBlank()) skillsMissing.add("skillsSummary");
        if (profile.getYearsOfExperience() == null) skillsMissing.add("yearsOfExperience");
        int skillsWeight = 15;
        boolean skillsComplete = skillsMissing.isEmpty();
        int skillsEarned = skillsComplete ? skillsWeight : (int)(skillsWeight * (2 - skillsMissing.size()) / 2.0);
        totalWeight += skillsWeight;
        earnedWeight += skillsEarned;
        sections.add(ProfileCompletionResponse.SectionCompletion.builder()
                .section("skills").label("Skills & Experience")
                .completed(skillsComplete).weight(skillsWeight)
                .missingFields(skillsMissing).build());

        // Education (weight: 10)
        List<String> eduMissing = new ArrayList<>();
        if (profile.getEducationSummary() == null || profile.getEducationSummary().isBlank()) eduMissing.add("educationSummary");
        int eduWeight = 10;
        boolean eduComplete = eduMissing.isEmpty();
        totalWeight += eduWeight;
        earnedWeight += eduComplete ? eduWeight : 0;
        sections.add(ProfileCompletionResponse.SectionCompletion.builder()
                .section("education").label("Education")
                .completed(eduComplete).weight(eduWeight)
                .missingFields(eduMissing).build());

        // Resume (weight: 20)
        List<String> resumeMissing = new ArrayList<>();
        boolean hasResume = resumeRepository.existsByCandidateAndActiveTrue(user);
        if (!hasResume) resumeMissing.add("activeResume");
        int resumeWeight = 20;
        totalWeight += resumeWeight;
        earnedWeight += hasResume ? resumeWeight : 0;
        sections.add(ProfileCompletionResponse.SectionCompletion.builder()
                .section("resume").label("Resume")
                .completed(hasResume).weight(resumeWeight)
                .missingFields(resumeMissing).build());

        // Social Links (weight: 10)
        List<String> linksMissing = new ArrayList<>();
        if (profile.getLinkedinUrl() == null || profile.getLinkedinUrl().isBlank()) linksMissing.add("linkedin");
        if (profile.getGithubUrl() == null || profile.getGithubUrl().isBlank()) linksMissing.add("github");
        if (profile.getPortfolioUrl() == null || profile.getPortfolioUrl().isBlank()) linksMissing.add("portfolio");
        int linksWeight = 10;
        int linksFilled = 3 - linksMissing.size();
        boolean linksComplete = linksFilled == 3;
        int linksEarned = (int)(linksWeight * linksFilled / 3.0);
        totalWeight += linksWeight;
        earnedWeight += linksEarned;
        sections.add(ProfileCompletionResponse.SectionCompletion.builder()
                .section("links").label("Professional Links")
                .completed(linksComplete).weight(linksWeight)
                .missingFields(linksMissing).build());

        // Profile Image (weight: 10)
        List<String> imageMissing = new ArrayList<>();
        boolean hasImage = profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isBlank();
        if (!hasImage) imageMissing.add("profileImage");
        int imageWeight = 10;
        totalWeight += imageWeight;
        earnedWeight += hasImage ? imageWeight : 0;
        sections.add(ProfileCompletionResponse.SectionCompletion.builder()
                .section("image").label("Profile Image")
                .completed(hasImage).weight(imageWeight)
                .missingFields(imageMissing).build());

        int overallPercentage = totalWeight > 0 ? (int)(earnedWeight * 100.0 / totalWeight) : 0;

        return ProfileCompletionResponse.builder()
                .overallPercentage(overallPercentage)
                .sections(sections)
                .build();
    }

    private void applyUpdate(CandidateProfile profile, CandidateProfileUpdateRequest request) {
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getHeadline() != null) profile.setHeadline(request.getHeadline());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getCurrentJobTitle() != null) profile.setCurrentJobTitle(request.getCurrentJobTitle());
        if (request.getYearsOfExperience() != null) profile.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getEducationSummary() != null) profile.setEducationSummary(request.getEducationSummary());
        if (request.getSkillsSummary() != null) profile.setSkillsSummary(request.getSkillsSummary());
        if (request.getLinkedinUrl() != null) profile.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl() != null) profile.setGithubUrl(request.getGithubUrl());
        if (request.getPortfolioUrl() != null) profile.setPortfolioUrl(request.getPortfolioUrl());
        if (request.getProfileImageUrl() != null) profile.setProfileImageUrl(request.getProfileImageUrl());
    }

    private CandidateProfileResponse mapToResponse(CandidateProfile profile) {
        User user = profile.getUser();
        return CandidateProfileResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(profile.getPhone())
                .location(profile.getLocation())
                .headline(profile.getHeadline())
                .bio(profile.getBio())
                .currentJobTitle(profile.getCurrentJobTitle())
                .yearsOfExperience(profile.getYearsOfExperience())
                .educationSummary(profile.getEducationSummary())
                .skillsSummary(profile.getSkillsSummary())
                .linkedinUrl(profile.getLinkedinUrl())
                .githubUrl(profile.getGithubUrl())
                .portfolioUrl(profile.getPortfolioUrl())
                .profileImageUrl(profile.getProfileImageUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
