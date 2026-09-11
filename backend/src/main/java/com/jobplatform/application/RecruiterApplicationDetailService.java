package com.jobplatform.application;

import com.jobplatform.application.dto.ApplicationNoteResponse;
import com.jobplatform.application.dto.RecruiterApplicationDetailResponse;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.candidate.CandidateProfile;
import com.jobplatform.candidate.CandidateProfileRepository;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.resume.Resume;
import com.jobplatform.resume.ResumeProfileData;
import com.jobplatform.resume.ResumeRepository;
import com.jobplatform.resume.enums.ResumeParsingStatus;
import com.jobplatform.resume.parsing.ResumeProfileDataRepository;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecruiterApplicationDetailService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final ApplicationNoteRepository noteRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeProfileDataRepository resumeProfileDataRepository;

    public RecruiterApplicationDetailService(
            ApplicationRepository applicationRepository,
            ApplicationStatusHistoryRepository historyRepository,
            ApplicationNoteRepository noteRepository,
            CandidateProfileRepository candidateProfileRepository,
            ResumeRepository resumeRepository,
            ResumeProfileDataRepository resumeProfileDataRepository) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.noteRepository = noteRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.resumeRepository = resumeRepository;
        this.resumeProfileDataRepository = resumeProfileDataRepository;
    }

    @Transactional(readOnly = true)
    public RecruiterApplicationDetailResponse getApplicationDetail(User recruiter, Long applicationId) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can view application details");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to view this application");
        }

        User candidate = application.getCandidate();

        RecruiterApplicationDetailResponse.CandidateInfo candidateInfo = buildCandidateInfo(candidate);

        List<RecruiterApplicationDetailResponse.StatusHistoryEntry> history = buildStatusHistory(applicationId);

        List<ApplicationNoteResponse> notes = buildNotes(applicationId, recruiter);

        Long resumeId = null;
        String resumeFileName = null;
        String resumeContentType = null;
        Long resumeFileSize = null;
        if (application.getSubmittedResume() != null) {
            Resume resume = application.getSubmittedResume();
            resumeId = resume.getId();
            resumeFileName = resume.getOriginalFileName();
            resumeContentType = resume.getContentType();
            resumeFileSize = resume.getFileSize();
        }

        return RecruiterApplicationDetailResponse.builder()
                .applicationId(application.getId())
                .status(application.getStatus())
                .coverLetter(application.getCoverLetter())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .resumeId(resumeId)
                .resumeFileName(resumeFileName)
                .resumeContentType(resumeContentType)
                .resumeFileSize(resumeFileSize)
                .candidate(candidateInfo)
                .statusHistory(history)
                .notes(notes)
                .build();
    }

    private RecruiterApplicationDetailResponse.CandidateInfo buildCandidateInfo(User candidate) {
        RecruiterApplicationDetailResponse.CandidateInfo.CandidateInfoBuilder builder =
                RecruiterApplicationDetailResponse.CandidateInfo.builder()
                        .candidateId(candidate.getId())
                        .fullName(candidate.getFullName())
                        .email(candidate.getEmail());

        Optional<CandidateProfile> profileOpt = candidateProfileRepository.findByUser(candidate);
        if (profileOpt.isPresent()) {
            CandidateProfile profile = profileOpt.get();
            builder.headline(profile.getHeadline())
                    .location(profile.getLocation())
                    .bio(profile.getBio())
                    .currentJobTitle(profile.getCurrentJobTitle())
                    .yearsOfExperience(profile.getYearsOfExperience())
                    .skillsSummary(profile.getSkillsSummary())
                    .educationSummary(profile.getEducationSummary())
                    .linkedinUrl(profile.getLinkedinUrl())
                    .githubUrl(profile.getGithubUrl())
                    .portfolioUrl(profile.getPortfolioUrl());
        }

        List<Resume> activeResumes = resumeRepository.findByCandidateAndActiveTrue(candidate);
        if (!activeResumes.isEmpty()) {
            Resume activeResume = activeResumes.get(0);
            if (activeResume.getParsingStatus() == ResumeParsingStatus.COMPLETED) {
                Optional<ResumeProfileData> parsedData = resumeProfileDataRepository.findByResume(activeResume);
                if (parsedData.isPresent()) {
                    ResumeProfileData data = parsedData.get();
                    builder.parsedSkills(data.getSkills())
                            .parsedYearsOfExperience(data.getTotalYearsOfExperience())
                            .professionalSummary(data.getProfessionalSummary());
                }
            }
        }

        return builder.build();
    }

    private List<RecruiterApplicationDetailResponse.StatusHistoryEntry> buildStatusHistory(Long applicationId) {
        List<ApplicationStatusHistory> historyEntities = historyRepository.findByApplicationIdOrderByChangedAtAsc(applicationId);
        List<RecruiterApplicationDetailResponse.StatusHistoryEntry> entries = new ArrayList<>();
        for (ApplicationStatusHistory h : historyEntities) {
            entries.add(RecruiterApplicationDetailResponse.StatusHistoryEntry.builder()
                    .id(h.getId())
                    .oldStatus(h.getOldStatus())
                    .newStatus(h.getNewStatus())
                    .changedByName(h.getChangedBy().getFullName())
                    .changedAt(h.getChangedAt())
                    .build());
        }
        return entries;
    }

    private List<ApplicationNoteResponse> buildNotes(Long applicationId, User recruiter) {
        return noteRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId)
                .stream()
                .filter(n -> n.getRecruiter().getId().equals(recruiter.getId()))
                .map(n -> ApplicationNoteResponse.builder()
                        .id(n.getId())
                        .note(n.getNote())
                        .recruiterName(n.getRecruiter().getFullName())
                        .createdAt(n.getCreatedAt())
                        .updatedAt(n.getUpdatedAt())
                        .build())
                .toList();
    }
}
