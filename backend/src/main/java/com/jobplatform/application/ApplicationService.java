package com.jobplatform.application;

import com.jobplatform.application.dto.ApplicationResponse;
import com.jobplatform.application.dto.ApplicationStats;
import com.jobplatform.application.dto.ApplicationSummaryResponse;
import com.jobplatform.application.dto.CandidateApplicationDetailResponse;
import com.jobplatform.application.dto.CandidateApplicationStatsResponse;
import com.jobplatform.application.dto.CandidateApplicationSummaryResponse;
import com.jobplatform.application.dto.CreateApplicationRequest;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.notification.NotificationService;
import com.jobplatform.notification.NotificationType;
import com.jobplatform.recruiter.RecruiterProfile;
import com.jobplatform.recruiter.RecruiterProfileRepository;
import com.jobplatform.resume.Resume;
import com.jobplatform.resume.ResumeService;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);
    private static final int MAX_PAGE_SIZE = 50;

    private static final Map<ApplicationStatus, List<ApplicationStatus>> VALID_TRANSITIONS = Map.of(
            ApplicationStatus.APPLIED, List.of(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.UNDER_REVIEW, List.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.SHORTLISTED, List.of(ApplicationStatus.INTERVIEW, ApplicationStatus.UNDER_REVIEW, ApplicationStatus.HIRED, ApplicationStatus.REJECTED),
            ApplicationStatus.INTERVIEW, List.of(ApplicationStatus.HIRED, ApplicationStatus.REJECTED, ApplicationStatus.SHORTLISTED),
            ApplicationStatus.HIRED, List.of(ApplicationStatus.UNDER_REVIEW)
    );

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final ResumeService resumeService;
    private final ApplicationStatusHistoryService statusHistoryService;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository, RecruiterProfileRepository recruiterProfileRepository, ResumeService resumeService, ApplicationStatusHistoryService statusHistoryService, NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.resumeService = resumeService;
        this.statusHistoryService = statusHistoryService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ApplicationResponse applyToJob(User candidate, Long jobId, CreateApplicationRequest request) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can apply to jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new BadRequestException("Can only apply to published jobs");
        }

        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Application deadline has passed");
        }

        if (applicationRepository.existsByCandidateAndJob(candidate, job)) {
            throw new BadRequestException("You have already applied to this job");
        }

        Optional<Resume> activeResume = resumeService.getResumeForApplication(candidate);

        Application application = Application.builder()
                .candidate(candidate)
                .job(job)
                .status(ApplicationStatus.APPLIED)
                .coverLetter(request.getCoverLetter() != null ? request.getCoverLetter().trim() : null)
                .submittedResume(activeResume.orElse(null))
                .build();

        Application saved = applicationRepository.save(application);
        log.info("Application created: candidate={} for job={}", candidate.getEmail(), job.getTitle());

        notificationService.createNotification(
                job.getRecruiter(),
                NotificationType.APPLICATION_RECEIVED,
                "New Application Received",
                candidate.getFullName() + " applied to " + job.getTitle(),
                saved.getId(),
                "APPLICATION"
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApplicationSummaryResponse> getCandidateApplications(User candidate, int page, int size, String sort) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can view their applications");
        }

        Pageable pageable = buildPageable(page, size, sort);
        Page<Application> applications = applicationRepository.findByCandidateOrderByAppliedAtDesc(candidate, pageable);

        return mapToSummaryPagedResponse(applications);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getCandidateApplicationById(User candidate, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new BadRequestException("You do not have permission to view this application");
        }

        return mapToResponse(application);
    }

    @Transactional
    public ApplicationResponse withdrawApplication(User candidate, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new BadRequestException("You do not have permission to modify this application");
        }

        if (application.getStatus() != ApplicationStatus.APPLIED && application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new BadRequestException("Application cannot be withdrawn in its current status");
        }

        application.setStatus(ApplicationStatus.WITHDRAWN);
        application.setWithdrawnAt(LocalDateTime.now());

        Application saved = applicationRepository.save(application);
        log.info("Application withdrawn: candidate={} for job={}", candidate.getEmail(), saved.getJob().getTitle());

        notificationService.createNotification(
                saved.getJob().getRecruiter(),
                NotificationType.APPLICATION_STATUS_CHANGED,
                "Application Withdrawn",
                candidate.getFullName() + " withdrew their application for " + saved.getJob().getTitle(),
                saved.getId(),
                "APPLICATION"
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<CandidateApplicationSummaryResponse> searchCandidateApplications(
            User candidate, String query, ApplicationStatus status, int page, int size, String sort) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can view their applications");
        }

        Pageable pageable = buildPageable(page, size, sort);
        Page<Application> applications = applicationRepository.searchCandidateApplications(candidate, query, status, pageable);

        return PagedResponse.<CandidateApplicationSummaryResponse>builder()
                .content(applications.getContent().stream().map(this::mapToCandidateSummaryResponse).toList())
                .page(applications.getNumber())
                .size(applications.getSize())
                .totalElements(applications.getTotalElements())
                .totalPages(applications.getTotalPages())
                .last(applications.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public CandidateApplicationStatsResponse getCandidateApplicationStats(User candidate) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can view stats");
        }

        return CandidateApplicationStatsResponse.builder()
                .totalApplications(applicationRepository.countByCandidate(candidate))
                .appliedCount(applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.APPLIED))
                .underReviewCount(applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.UNDER_REVIEW))
                .shortlistedCount(applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.SHORTLISTED))
                .rejectedCount(applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.REJECTED))
                .hiredCount(applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.HIRED))
                .withdrawnCount(applicationRepository.countByCandidateAndStatus(candidate, ApplicationStatus.WITHDRAWN))
                .build();
    }

    @Transactional(readOnly = true)
    public CandidateApplicationDetailResponse getCandidateApplicationDetail(User candidate, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new BadRequestException("You do not have permission to view this application");
        }

        List<ApplicationStatusHistory> history = statusHistoryService.getHistory(applicationId);

        return CandidateApplicationDetailResponse.builder()
                .applicationId(application.getId())
                .status(application.getStatus())
                .coverLetter(application.getCoverLetter())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .withdrawnAt(application.getWithdrawnAt())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .jobDescription(application.getJob().getDescription())
                .location(application.getJob().getLocation())
                .employmentType(application.getJob().getEmploymentType().name())
                .workplaceType(application.getJob().getWorkplaceType().name())
                .experienceMin(application.getJob().getExperienceMin())
                .experienceMax(application.getJob().getExperienceMax())
                .salaryMin(application.getJob().getSalaryMin())
                .salaryMax(application.getJob().getSalaryMax())
                .skills(application.getJob().getSkills())
                .deadline(application.getJob().getApplicationDeadline())
                .resumeId(application.getSubmittedResume() != null ? application.getSubmittedResume().getId() : null)
                .resumeFileName(application.getSubmittedResume() != null ? application.getSubmittedResume().getOriginalFileName() : null)
                .resumeContentType(application.getSubmittedResume() != null ? application.getSubmittedResume().getContentType() : null)
                .resumeFileSize(application.getSubmittedResume() != null ? application.getSubmittedResume().getFileSize() : null)
                .statusHistory(history.stream().map(h -> CandidateApplicationDetailResponse.StatusHistoryEntry.builder()
                        .id(h.getId())
                        .oldStatus(h.getOldStatus())
                        .newStatus(h.getNewStatus())
                        .changedByName(h.getChangedBy().getFullName())
                        .changedAt(h.getChangedAt())
                        .reason(h.getReason())
                        .build()).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApplicationSummaryResponse> getRecruiterApplications(User recruiter, int page, int size, String sort) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can view applications");
        }

        Pageable pageable = buildPageable(page, size, sort);
        Page<Application> applications = applicationRepository.findByJobRecruiterOrderByAppliedAtDesc(recruiter, pageable);

        return mapToSummaryPagedResponse(applications);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApplicationSummaryResponse> searchRecruiterApplications(
            User recruiter, String query, Long jobId, ApplicationStatus status, int page, int size, String sort) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can view applications");
        }

        Pageable pageable = buildPageable(page, size, sort);
        Page<Application> applications = applicationRepository.searchRecruiterApplicationsFiltered(
                recruiter, query, jobId, status, pageable);

        return mapToSummaryPagedResponse(applications);
    }

    @Transactional(readOnly = true)
    public com.jobplatform.application.dto.RecruiterAggregateStatsResponse getRecruiterAggregateStats(User recruiter) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can view stats");
        }

        return com.jobplatform.application.dto.RecruiterAggregateStatsResponse.builder()
                .totalApplications(applicationRepository.countByJobRecruiter(recruiter))
                .appliedCount(applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.APPLIED))
                .underReviewCount(applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.UNDER_REVIEW))
                .shortlistedCount(applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.SHORTLISTED))
                .rejectedCount(applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.REJECTED))
                .hiredCount(applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.HIRED))
                .withdrawnCount(applicationRepository.countByJobRecruiterAndStatus(recruiter, ApplicationStatus.WITHDRAWN))
                .totalJobs((int) jobRepository.countByRecruiter(recruiter))
                .build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<ApplicationSummaryResponse> getRecruiterJobApplications(User recruiter, Long jobId, int page, int size, String sort) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can view applications");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to view applications for this job");
        }

        Pageable pageable = buildPageable(page, size, sort);
        Page<Application> applications = applicationRepository.findByJobAndJobRecruiterOrderByAppliedAtDesc(job, recruiter, pageable);

        return mapToSummaryPagedResponse(applications);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getRecruiterApplicationById(User recruiter, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to view this application");
        }

        return mapToResponse(application);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(User recruiter, Long applicationId, ApplicationStatus newStatus) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can update application status");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to modify this application");
        }

        if (!isValidTransition(application.getStatus(), newStatus)) {
            throw new BadRequestException("Invalid status transition from " + application.getStatus() + " to " + newStatus);
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(newStatus);

        Application saved = applicationRepository.save(application);
        statusHistoryService.recordTransition(saved, oldStatus, newStatus, recruiter);
        log.info("Application status updated: application={} from {} to {}", applicationId, oldStatus, newStatus);

        String statusMessage = switch (newStatus) {
            case UNDER_REVIEW -> "is now under review";
            case SHORTLISTED -> "has been shortlisted";
            case INTERVIEW -> "has been moved to interview stage";
            case REJECTED -> "has been rejected";
            case HIRED -> "has been accepted - Congratulations!";
            default -> "status has been updated to " + newStatus;
        };
        notificationService.createNotification(
                application.getCandidate(),
                NotificationType.APPLICATION_STATUS_CHANGED,
                "Application Status Updated",
                "Your application for " + application.getJob().getTitle() + " " + statusMessage,
                saved.getId(),
                "APPLICATION"
        );

        return mapToResponse(saved);
    }

    @Transactional
    public ApplicationResponse unhireApplication(User recruiter, Long applicationId, String reason) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can unhire applications");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to modify this application");
        }

        if (application.getStatus() != ApplicationStatus.HIRED) {
            throw new BadRequestException("Application is not in HIRED status");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.UNDER_REVIEW);

        Application saved = applicationRepository.save(application);
        statusHistoryService.recordTransition(saved, oldStatus, ApplicationStatus.UNDER_REVIEW, recruiter, reason);
        log.info("Application unhired: application={} by recruiter={} reason={}", applicationId, recruiter.getEmail(), reason);

        notificationService.createNotification(
                application.getCandidate(),
                NotificationType.APPLICATION_STATUS_CHANGED,
                "Application Status Updated",
                "Your application for " + application.getJob().getTitle() + " is no longer marked as hired and is now under review again.",
                saved.getId(),
                "APPLICATION"
        );

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ApplicationStats getRecruiterJobStats(User recruiter, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to view stats for this job");
        }

        return ApplicationStats.builder()
                .totalApplications(applicationRepository.countByJob(job))
                .appliedCount(applicationRepository.countByJobAndStatus(job, ApplicationStatus.APPLIED))
                .underReviewCount(applicationRepository.countByJobAndStatus(job, ApplicationStatus.UNDER_REVIEW))
                .shortlistedCount(applicationRepository.countByJobAndStatus(job, ApplicationStatus.SHORTLISTED))
                .rejectedCount(applicationRepository.countByJobAndStatus(job, ApplicationStatus.REJECTED))
                .hiredCount(applicationRepository.countByJobAndStatus(job, ApplicationStatus.HIRED))
                .withdrawnCount(applicationRepository.countByJobAndStatus(job, ApplicationStatus.WITHDRAWN))
                .build();
    }

    private boolean isValidTransition(ApplicationStatus current, ApplicationStatus next) {
        List<ApplicationStatus> allowed = VALID_TRANSITIONS.get(current);
        return allowed != null && allowed.contains(next);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        Sort sortBy = switch (sort != null ? sort : "newest") {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "appliedAt");
            case "updated" -> Sort.by(Sort.Direction.DESC, "updatedAt");
            default -> Sort.by(Sort.Direction.DESC, "appliedAt");
        };

        return PageRequest.of(safePage, safeSize, sortBy);
    }

    private ApplicationResponse mapToResponse(Application application) {
        String companyName = resolveCompanyName(application.getJob());
        return ApplicationResponse.builder()
                .id(application.getId())
                .candidateId(application.getCandidate().getId())
                .candidateName(application.getCandidate().getFullName())
                .candidateEmail(application.getCandidate().getEmail())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .companyName(companyName)
                .status(application.getStatus())
                .coverLetter(application.getCoverLetter())
                .submittedResumeId(application.getSubmittedResume() != null ? application.getSubmittedResume().getId() : null)
                .submittedResumeName(application.getSubmittedResume() != null ? application.getSubmittedResume().getOriginalFileName() : null)
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .withdrawnAt(application.getWithdrawnAt())
                .build();
    }

    private ApplicationSummaryResponse mapToSummaryResponse(Application application) {
        String companyName = resolveCompanyName(application.getJob());
        return ApplicationSummaryResponse.builder()
                .id(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .companyName(companyName)
                .candidateName(application.getCandidate().getFullName())
                .candidateEmail(application.getCandidate().getEmail())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    private String resolveCompanyName(Job job) {
        try {
            return recruiterProfileRepository.findByUserId(job.getRecruiter().getId())
                    .map(RecruiterProfile::getCompanyName)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private PagedResponse<ApplicationSummaryResponse> mapToSummaryPagedResponse(Page<Application> applications) {
        return PagedResponse.<ApplicationSummaryResponse>builder()
                .content(applications.getContent().stream().map(this::mapToSummaryResponse).toList())
                .page(applications.getNumber())
                .size(applications.getSize())
                .totalElements(applications.getTotalElements())
                .totalPages(applications.getTotalPages())
                .last(applications.isLast())
                .build();
    }

    private CandidateApplicationSummaryResponse mapToCandidateSummaryResponse(Application application) {
        return CandidateApplicationSummaryResponse.builder()
                .applicationId(application.getId())
                .jobId(application.getJob().getId())
                .jobTitle(application.getJob().getTitle())
                .location(application.getJob().getLocation())
                .employmentType(application.getJob().getEmploymentType().name())
                .workplaceType(application.getJob().getWorkplaceType().name())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .hasResume(application.getSubmittedResume() != null)
                .resumeFileName(application.getSubmittedResume() != null ? application.getSubmittedResume().getOriginalFileName() : null)
                .deadline(application.getJob().getApplicationDeadline())
                .build();
    }
}
