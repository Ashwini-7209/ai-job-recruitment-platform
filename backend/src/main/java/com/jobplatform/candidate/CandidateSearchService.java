package com.jobplatform.candidate;

import com.jobplatform.candidate.dto.CandidateSearchResultResponse;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.resume.Resume;
import com.jobplatform.resume.ResumeRepository;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CandidateSearchService {

    private static final int MAX_PAGE_SIZE = 50;

    private final CandidateProfileRepository candidateProfileRepository;
    private final ResumeRepository resumeRepository;

    public CandidateSearchService(CandidateProfileRepository candidateProfileRepository,
                                   ResumeRepository resumeRepository) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.resumeRepository = resumeRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<CandidateSearchResultResponse> searchCandidates(
            User recruiter, String query, String location, String skills,
            Integer minExperience, Integer maxExperience, String jobTitle,
            int page, int size, String sort) {

        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can search candidates");
        }

        Pageable pageable = buildPageable(page, size, sort);
        Page<CandidateProfile> profiles = candidateProfileRepository.searchCandidates(
                query, location, skills, minExperience, maxExperience, jobTitle, pageable);

        List<Long> userIds = profiles.getContent().stream()
                .map(cp -> cp.getUser().getId())
                .toList();

        Set<Long> resumeUserIds = Set.of();
        if (!userIds.isEmpty()) {
            resumeUserIds = resumeRepository.findCandidateIdsWithActiveResume(userIds)
                    .stream().collect(Collectors.toSet());
        }

        Set<Long> finalResumeUserIds = resumeUserIds;
        List<CandidateSearchResultResponse> content = profiles.getContent().stream()
                .map(cp -> mapToResponse(cp, finalResumeUserIds.contains(cp.getUser().getId())))
                .toList();

        return PagedResponse.<CandidateSearchResultResponse>builder()
                .content(content)
                .page(profiles.getNumber())
                .size(profiles.getSize())
                .totalElements(profiles.getTotalElements())
                .totalPages(profiles.getTotalPages())
                .last(profiles.isLast())
                .build();
    }

    private Pageable buildPageable(int page, int size, String sort) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        Sort sortBy = switch (sort != null ? sort : "name_asc") {
            case "name_desc" -> Sort.by(Sort.Direction.DESC, "user.fullName");
            case "experience_high" -> Sort.by(Sort.Direction.DESC, "yearsOfExperience");
            case "experience_low" -> Sort.by(Sort.Direction.ASC, "yearsOfExperience");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.ASC, "user.fullName");
        };

        return PageRequest.of(safePage, safeSize, sortBy);
    }

    private CandidateSearchResultResponse mapToResponse(CandidateProfile cp, boolean hasResume) {
        return CandidateSearchResultResponse.builder()
                .candidateId(cp.getId())
                .userId(cp.getUser().getId())
                .fullName(cp.getUser().getFullName())
                .email(cp.getUser().getEmail())
                .headline(cp.getHeadline())
                .location(cp.getLocation())
                .currentJobTitle(cp.getCurrentJobTitle())
                .yearsOfExperience(cp.getYearsOfExperience())
                .skillsSummary(cp.getSkillsSummary())
                .educationSummary(cp.getEducationSummary())
                .bio(cp.getBio())
                .profileImageUrl(cp.getProfileImageUrl())
                .linkedinUrl(cp.getLinkedinUrl())
                .githubUrl(cp.getGithubUrl())
                .hasResume(hasResume)
                .build();
    }
}
