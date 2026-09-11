package com.jobplatform.recruiter;

import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.recruiter.dto.RecruiterProfileResponse;
import com.jobplatform.recruiter.dto.RecruiterProfileUpdateRequest;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecruiterProfileService {

    private static final Logger log = LoggerFactory.getLogger(RecruiterProfileService.class);

    private final RecruiterProfileRepository recruiterProfileRepository;

    public RecruiterProfileService(RecruiterProfileRepository recruiterProfileRepository) {
        this.recruiterProfileRepository = recruiterProfileRepository;
    }

    @Transactional(readOnly = true)
    public RecruiterProfile getProfileByUserId(Long userId) {
        return recruiterProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile", "userId", userId));
    }

    @Transactional(readOnly = true)
    public RecruiterProfileResponse getProfileResponse(Long userId) {
        RecruiterProfile profile = getProfileByUserId(userId);
        return mapToResponse(profile);
    }

    @Transactional
    public RecruiterProfile createProfile(User user) {
        if (user.getRole() != UserRole.RECRUITER) {
            throw new IllegalArgumentException("Only RECRUITER users can have a recruiter profile");
        }
        RecruiterProfile profile = RecruiterProfile.builder()
                .user(user)
                .build();
        RecruiterProfile saved = recruiterProfileRepository.save(profile);
        log.info("Created recruiter profile for user: {}", user.getEmail());
        return saved;
    }

    @Transactional
    public RecruiterProfileResponse updateProfile(Long userId, RecruiterProfileUpdateRequest request) {
        RecruiterProfile profile = getProfileByUserId(userId);
        applyUpdate(profile, request);
        RecruiterProfile saved = recruiterProfileRepository.save(profile);
        log.info("Updated recruiter profile for userId: {}", userId);
        return mapToResponse(saved);
    }

    private void applyUpdate(RecruiterProfile profile, RecruiterProfileUpdateRequest request) {
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getJobTitle() != null) profile.setJobTitle(request.getJobTitle());
        if (request.getDepartment() != null) profile.setDepartment(request.getDepartment());
        if (request.getCompanyName() != null) profile.setCompanyName(request.getCompanyName());
        if (request.getCompanyWebsite() != null) profile.setCompanyWebsite(request.getCompanyWebsite());
        if (request.getCompanyDescription() != null) profile.setCompanyDescription(request.getCompanyDescription());
        if (request.getCompanyLocation() != null) profile.setCompanyLocation(request.getCompanyLocation());
        if (request.getLinkedinUrl() != null) profile.setLinkedinUrl(request.getLinkedinUrl());
    }

    private RecruiterProfileResponse mapToResponse(RecruiterProfile profile) {
        User user = profile.getUser();
        return RecruiterProfileResponse.builder()
                .id(profile.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(profile.getPhone())
                .jobTitle(profile.getJobTitle())
                .department(profile.getDepartment())
                .companyName(profile.getCompanyName())
                .companyWebsite(profile.getCompanyWebsite())
                .companyDescription(profile.getCompanyDescription())
                .companyLocation(profile.getCompanyLocation())
                .linkedinUrl(profile.getLinkedinUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
