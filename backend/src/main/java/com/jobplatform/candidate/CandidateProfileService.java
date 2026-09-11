package com.jobplatform.candidate;

import com.jobplatform.candidate.dto.CandidateProfileResponse;
import com.jobplatform.candidate.dto.CandidateProfileUpdateRequest;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateProfileService {

    private static final Logger log = LoggerFactory.getLogger(CandidateProfileService.class);

    private final CandidateProfileRepository candidateProfileRepository;

    public CandidateProfileService(CandidateProfileRepository candidateProfileRepository) {
        this.candidateProfileRepository = candidateProfileRepository;
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
