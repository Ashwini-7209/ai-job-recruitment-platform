package com.jobplatform.interview;

import com.jobplatform.application.Application;
import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.interview.dto.CandidateInterviewResponse;
import com.jobplatform.interview.dto.CreateInterviewRequest;
import com.jobplatform.interview.dto.RecruiterInterviewResponse;
import com.jobplatform.interview.dto.UpdateInterviewRequest;
import com.jobplatform.interview.enums.InterviewStatus;
import com.jobplatform.notification.NotificationService;
import com.jobplatform.notification.NotificationType;
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
import java.util.List;
import java.util.Map;

@Service
public class InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewService.class);
    private static final int MAX_PAGE_SIZE = 50;

    private static final Map<InterviewStatus, List<InterviewStatus>> VALID_TRANSITIONS = Map.of(
            InterviewStatus.SCHEDULED, List.of(InterviewStatus.RESCHEDULED, InterviewStatus.COMPLETED, InterviewStatus.CANCELLED),
            InterviewStatus.RESCHEDULED, List.of(InterviewStatus.RESCHEDULED, InterviewStatus.COMPLETED, InterviewStatus.CANCELLED)
    );

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;

    public InterviewService(InterviewRepository interviewRepository, ApplicationRepository applicationRepository, NotificationService notificationService) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.notificationService = notificationService;
    }

    // ==================== RECRUITER METHODS ====================

    @Transactional
    public RecruiterInterviewResponse createInterview(User recruiter, Long applicationId, CreateInterviewRequest request) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can create interviews");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        if (!application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to schedule interviews for this application");
        }

        validateSchedule(request.getScheduledStart(), request.getScheduledEnd());

        if (interviewRepository.existsByApplicationAndScheduledStartLessThanEqualAndScheduledEndGreaterThanEqualAndStatusNot(
                application, request.getScheduledEnd(), request.getScheduledStart(), InterviewStatus.CANCELLED)) {
            throw new BadRequestException("This application already has an interview scheduled during this time slot");
        }

        Interview interview = Interview.builder()
                .application(application)
                .title(request.getTitle().trim())
                .interviewType(request.getInterviewType())
                .scheduledStart(request.getScheduledStart())
                .scheduledEnd(request.getScheduledEnd())
                .location(request.getLocation() != null ? request.getLocation().trim() : null)
                .meetingLink(request.getMeetingLink() != null ? request.getMeetingLink().trim() : null)
                .interviewerName(request.getInterviewerName() != null ? request.getInterviewerName().trim() : null)
                .interviewerNotes(request.getInterviewerNotes() != null ? request.getInterviewerNotes().trim() : null)
                .candidateNotes(request.getCandidateNotes() != null ? request.getCandidateNotes().trim() : null)
                .status(InterviewStatus.SCHEDULED)
                .build();

        Interview saved = interviewRepository.save(interview);
        log.info("Interview created: id={} for application={} by recruiter={}", saved.getId(), applicationId, recruiter.getEmail());

        notificationService.createNotification(
                application.getCandidate(),
                NotificationType.INTERVIEW_SCHEDULED,
                "Interview Scheduled",
                "You have an interview scheduled for " + request.getTitle() + " (" + request.getInterviewType() + ")",
                saved.getId(),
                "INTERVIEW"
        );

        return mapToRecruiterResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<RecruiterInterviewResponse> getRecruiterInterviews(User recruiter, int page, int size, String sort) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can view interviews");
        }

        Pageable pageable = buildPageable(page, size, sort);
        Page<Interview> interviews = interviewRepository.findByApplicationJobRecruiterOrderByScheduledStartDesc(recruiter, pageable);

        return PagedResponse.<RecruiterInterviewResponse>builder()
                .content(interviews.getContent().stream().map(this::mapToRecruiterResponse).toList())
                .page(interviews.getNumber())
                .size(interviews.getSize())
                .totalElements(interviews.getTotalElements())
                .totalPages(interviews.getTotalPages())
                .last(interviews.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<RecruiterInterviewResponse> getUpcomingRecruiterInterviews(User recruiter) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can view interviews");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfWeek = now.plusWeeks(2);

        List<Interview> interviews = interviewRepository.findUpcomingByRecruiter(recruiter, now, endOfWeek);
        return interviews.stream().map(this::mapToRecruiterResponse).toList();
    }

    @Transactional(readOnly = true)
    public RecruiterInterviewResponse getRecruiterInterviewById(User recruiter, Long interviewId) {
        Interview interview = interviewRepository.findByIdAndApplicationJobRecruiter(interviewId, recruiter)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));
        return mapToRecruiterResponse(interview);
    }

    @Transactional
    public RecruiterInterviewResponse updateInterview(User recruiter, Long interviewId, UpdateInterviewRequest request) {
        Interview interview = interviewRepository.findByIdAndApplicationJobRecruiter(interviewId, recruiter)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));

        if (request.getTitle() != null) {
            interview.setTitle(request.getTitle().trim());
        }
        if (request.getInterviewType() != null) {
            interview.setInterviewType(request.getInterviewType());
        }
        if (request.getScheduledStart() != null) {
            LocalDateTime newStart = request.getScheduledStart();
            LocalDateTime newEnd = request.getScheduledEnd() != null ? request.getScheduledEnd() : interview.getScheduledEnd();
            validateSchedule(newStart, newEnd);
            interview.setScheduledStart(newStart);
            interview.setScheduledEnd(newEnd);
            if (interview.getStatus() == InterviewStatus.SCHEDULED) {
                interview.setStatus(InterviewStatus.RESCHEDULED);
            }
        }
        if (request.getLocation() != null) {
            interview.setLocation(request.getLocation().trim());
        }
        if (request.getMeetingLink() != null) {
            interview.setMeetingLink(request.getMeetingLink().trim());
        }
        if (request.getInterviewerName() != null) {
            interview.setInterviewerName(request.getInterviewerName().trim());
        }
        if (request.getInterviewerNotes() != null) {
            interview.setInterviewerNotes(request.getInterviewerNotes().trim());
        }
        if (request.getCandidateNotes() != null) {
            interview.setCandidateNotes(request.getCandidateNotes().trim());
        }

        Interview saved = interviewRepository.save(interview);
        log.info("Interview updated: id={} by recruiter={}", interviewId, recruiter.getEmail());

        if (request.getScheduledStart() != null || request.getScheduledEnd() != null) {
            notificationService.createNotification(
                    interview.getApplication().getCandidate(),
                    NotificationType.INTERVIEW_RESCHEDULED,
                    "Interview Rescheduled",
                    "Your interview '" + saved.getTitle() + "' has been rescheduled",
                    saved.getId(),
                    "INTERVIEW"
            );
        }

        return mapToRecruiterResponse(saved);
    }

    @Transactional
    public RecruiterInterviewResponse cancelInterview(User recruiter, Long interviewId) {
        Interview interview = interviewRepository.findByIdAndApplicationJobRecruiter(interviewId, recruiter)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));

        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new BadRequestException("Interview is already cancelled");
        }

        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed interview");
        }

        interview.setStatus(InterviewStatus.CANCELLED);
        Interview saved = interviewRepository.save(interview);
        log.info("Interview cancelled: id={} by recruiter={}", interviewId, recruiter.getEmail());

        notificationService.createNotification(
                interview.getApplication().getCandidate(),
                NotificationType.INTERVIEW_CANCELLED,
                "Interview Cancelled",
                "Your interview '" + saved.getTitle() + "' has been cancelled",
                saved.getId(),
                "INTERVIEW"
        );

        return mapToRecruiterResponse(saved);
    }

    @Transactional
    public RecruiterInterviewResponse updateInterviewStatus(User recruiter, Long interviewId, InterviewStatus newStatus) {
        Interview interview = interviewRepository.findByIdAndApplicationJobRecruiter(interviewId, recruiter)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));

        if (!isValidTransition(interview.getStatus(), newStatus)) {
            throw new BadRequestException("Invalid status transition from " + interview.getStatus() + " to " + newStatus);
        }

        interview.setStatus(newStatus);
        Interview saved = interviewRepository.save(interview);
        log.info("Interview status updated: id={} from {} to {} by recruiter={}", interviewId, interview.getStatus(), newStatus, recruiter.getEmail());
        return mapToRecruiterResponse(saved);
    }

    @Transactional(readOnly = true)
    public long getRecruiterUpcomingCount(User recruiter) {
        return interviewRepository.countByApplicationJobRecruiterAndScheduledStartAfter(recruiter, LocalDateTime.now());
    }

    // ==================== CANDIDATE METHODS ====================

    @Transactional(readOnly = true)
    public PagedResponse<CandidateInterviewResponse> getCandidateInterviews(User candidate, int page, int size, String sort) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can view interviews");
        }

        Pageable pageable = buildPageable(page, size, sort);
        Page<Interview> interviews = interviewRepository.findByApplicationCandidateOrderByScheduledStartDesc(candidate, pageable);

        return PagedResponse.<CandidateInterviewResponse>builder()
                .content(interviews.getContent().stream().map(this::mapToCandidateResponse).toList())
                .page(interviews.getNumber())
                .size(interviews.getSize())
                .totalElements(interviews.getTotalElements())
                .totalPages(interviews.getTotalPages())
                .last(interviews.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CandidateInterviewResponse> getUpcomingCandidateInterviews(User candidate) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can view interviews");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfWeek = now.plusWeeks(2);

        List<Interview> interviews = interviewRepository.findUpcomingByCandidate(candidate, now, endOfWeek);
        return interviews.stream().map(this::mapToCandidateResponse).toList();
    }

    @Transactional(readOnly = true)
    public CandidateInterviewResponse getCandidateInterviewById(User candidate, Long interviewId) {
        Interview interview = interviewRepository.findByIdAndApplicationCandidate(interviewId, candidate)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));
        return mapToCandidateResponse(interview);
    }

    @Transactional(readOnly = true)
    public long getCandidateUpcomingCount(User candidate) {
        return interviewRepository.countByApplicationCandidateAndScheduledStartAfter(candidate, LocalDateTime.now());
    }

    // ==================== PRIVATE HELPERS ====================

    private void validateSchedule(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start) || end.isEqual(start)) {
            throw new BadRequestException("Interview end time must be after start time");
        }
    }

    private boolean isValidTransition(InterviewStatus current, InterviewStatus next) {
        List<InterviewStatus> allowed = VALID_TRANSITIONS.get(current);
        return allowed != null && allowed.contains(next);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        Sort sortBy = switch (sort != null ? sort : "newest") {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "scheduledStart");
            case "status" -> Sort.by(Sort.Direction.ASC, "status");
            default -> Sort.by(Sort.Direction.DESC, "scheduledStart");
        };

        return PageRequest.of(safePage, safeSize, sortBy);
    }

    private RecruiterInterviewResponse mapToRecruiterResponse(Interview interview) {
        Application app = interview.getApplication();
        return RecruiterInterviewResponse.builder()
                .interviewId(interview.getId())
                .applicationId(app.getId())
                .candidateId(app.getCandidate().getId())
                .candidateName(app.getCandidate().getFullName())
                .candidateEmail(app.getCandidate().getEmail())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .title(interview.getTitle())
                .interviewType(interview.getInterviewType())
                .scheduledStart(interview.getScheduledStart())
                .scheduledEnd(interview.getScheduledEnd())
                .location(interview.getLocation())
                .meetingLink(interview.getMeetingLink())
                .interviewerName(interview.getInterviewerName())
                .interviewerNotes(interview.getInterviewerNotes())
                .candidateNotes(interview.getCandidateNotes())
                .status(interview.getStatus())
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .build();
    }

    private CandidateInterviewResponse mapToCandidateResponse(Interview interview) {
        Application app = interview.getApplication();
        return CandidateInterviewResponse.builder()
                .interviewId(interview.getId())
                .applicationId(app.getId())
                .jobId(app.getJob().getId())
                .jobTitle(app.getJob().getTitle())
                .title(interview.getTitle())
                .interviewType(interview.getInterviewType())
                .scheduledStart(interview.getScheduledStart())
                .scheduledEnd(interview.getScheduledEnd())
                .location(interview.getLocation())
                .meetingLink(interview.getMeetingLink())
                .interviewerName(interview.getInterviewerName())
                .status(interview.getStatus())
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .build();
    }
}
