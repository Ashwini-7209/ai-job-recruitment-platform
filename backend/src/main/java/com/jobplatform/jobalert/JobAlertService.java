package com.jobplatform.jobalert;

import com.jobplatform.common.PagedResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.jobalert.dto.CreateJobAlertRequest;
import com.jobplatform.jobalert.dto.JobAlertResponse;
import com.jobplatform.jobalert.dto.UpdateJobAlertRequest;
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
import java.util.Arrays;
import java.util.List;

@Service
public class JobAlertService {

    private static final Logger log = LoggerFactory.getLogger(JobAlertService.class);
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_ACTIVE_ALERTS = 10;

    private final JobAlertRepository jobAlertRepository;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;

    public JobAlertService(JobAlertRepository jobAlertRepository, JobRepository jobRepository,
                           NotificationService notificationService) {
        this.jobAlertRepository = jobAlertRepository;
        this.jobRepository = jobRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public JobAlertResponse createAlert(User candidate, CreateJobAlertRequest request) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can create job alerts");
        }

        long activeCount = jobAlertRepository.countByCandidateAndActiveTrue(candidate);
        if (activeCount >= MAX_ACTIVE_ALERTS) {
            throw new BadRequestException("Maximum number of active alerts (" + MAX_ACTIVE_ALERTS + ") reached");
        }

        if (jobAlertRepository.existsByCandidateAndName(candidate, request.getName())) {
            throw new BadRequestException("An alert with this name already exists");
        }

        if (!hasAtLeastOneCriterion(request)) {
            throw new BadRequestException("At least one matching criterion must be provided");
        }

        JobAlert alert = JobAlert.builder()
                .candidate(candidate)
                .name(request.getName())
                .keywords(request.getKeywords())
                .location(request.getLocation())
                .workplaceType(request.getWorkplaceType())
                .employmentType(request.getEmploymentType())
                .minimumExperience(request.getMinimumExperience())
                .skills(request.getSkills())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        JobAlert saved = jobAlertRepository.save(alert);
        log.info("Job alert created: candidate={} alert={}", candidate.getEmail(), saved.getName());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<JobAlertResponse> getAlerts(User candidate, int page, int size) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can view job alerts");
        }

        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<JobAlert> alerts = jobAlertRepository.findByCandidateOrderByCreatedAtDesc(candidate, pageable);

        return PagedResponse.<JobAlertResponse>builder()
                .content(alerts.getContent().stream().map(this::mapToResponse).toList())
                .page(alerts.getNumber())
                .size(alerts.getSize())
                .totalElements(alerts.getTotalElements())
                .totalPages(alerts.getTotalPages())
                .last(alerts.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public JobAlertResponse getAlert(User candidate, Long alertId) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can view job alerts");
        }

        JobAlert alert = getAlertIfExists(candidate, alertId);
        return mapToResponse(alert);
    }

    @Transactional
    public JobAlertResponse updateAlert(User candidate, Long alertId, UpdateJobAlertRequest request) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can update job alerts");
        }

        JobAlert alert = getAlertIfExists(candidate, alertId);

        if (request.getName() != null && !request.getName().equals(alert.getName())) {
            if (jobAlertRepository.existsByCandidateAndName(candidate, request.getName())) {
                throw new BadRequestException("An alert with this name already exists");
            }
            alert.setName(request.getName());
        }

        if (request.getKeywords() != null) alert.setKeywords(request.getKeywords());
        if (request.getLocation() != null) alert.setLocation(request.getLocation());
        if (request.getWorkplaceType() != null) alert.setWorkplaceType(request.getWorkplaceType());
        if (request.getEmploymentType() != null) alert.setEmploymentType(request.getEmploymentType());
        if (request.getMinimumExperience() != null) alert.setMinimumExperience(request.getMinimumExperience());
        if (request.getSkills() != null) alert.setSkills(request.getSkills());
        if (request.getActive() != null) alert.setActive(request.getActive());

        JobAlert updated = jobAlertRepository.save(alert);
        log.info("Job alert updated: candidate={} alertId={}", candidate.getEmail(), alertId);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteAlert(User candidate, Long alertId) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can delete job alerts");
        }

        JobAlert alert = getAlertIfExists(candidate, alertId);
        jobAlertRepository.delete(alert);
        log.info("Job alert deleted: candidate={} alertId={}", candidate.getEmail(), alertId);
    }

    @Transactional
    public JobAlertResponse toggleAlertStatus(User candidate, Long alertId) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can toggle job alert status");
        }

        JobAlert alert = getAlertIfExists(candidate, alertId);

        if (!alert.getActive()) {
            long activeCount = jobAlertRepository.countByCandidateAndActiveTrue(candidate);
            if (activeCount >= MAX_ACTIVE_ALERTS) {
                throw new BadRequestException("Maximum number of active alerts (" + MAX_ACTIVE_ALERTS + ") reached");
            }
        }

        alert.setActive(!alert.getActive());
        JobAlert updated = jobAlertRepository.save(alert);
        log.info("Job alert status toggled: candidate={} alertId={} active={}", candidate.getEmail(), alertId, updated.getActive());
        return mapToResponse(updated);
    }

    @Transactional
    public void processNewJobPublication(Job job) {
        if (job.getStatus() != com.jobplatform.job.enums.JobStatus.PUBLISHED) {
            return;
        }

        List<JobAlert> activeAlerts = jobAlertRepository.findAllActiveAlerts();

        for (JobAlert alert : activeAlerts) {
            if (!alert.getCandidate().getId().equals(job.getRecruiter().getId())) {
                if (matchesAlert(job, alert)) {
                    createAlertNotification(alert.getCandidate(), job, alert);
                    alert.setLastTriggeredAt(LocalDateTime.now());
                    jobAlertRepository.save(alert);
                }
            }
        }
    }

    private boolean matchesAlert(Job job, JobAlert alert) {
        if (alert.getKeywords() != null && !alert.getKeywords().trim().isEmpty()) {
            String[] keywords = alert.getKeywords().toLowerCase().split(",");
            boolean matchesKeyword = Arrays.stream(keywords)
                    .map(String::trim)
                    .filter(k -> !k.isEmpty())
                    .anyMatch(keyword ->
                            job.getTitle().toLowerCase().contains(keyword) ||
                            job.getDescription().toLowerCase().contains(keyword) ||
                            (job.getSkills() != null && job.getSkills().toLowerCase().contains(keyword)));
            if (!matchesKeyword) return false;
        }

        if (alert.getLocation() != null && !alert.getLocation().trim().isEmpty()) {
            if (job.getLocation() == null ||
                !job.getLocation().toLowerCase().contains(alert.getLocation().toLowerCase().trim())) {
                return false;
            }
        }

        if (alert.getWorkplaceType() != null) {
            if (job.getWorkplaceType() != alert.getWorkplaceType()) {
                return false;
            }
        }

        if (alert.getEmploymentType() != null) {
            if (job.getEmploymentType() != alert.getEmploymentType()) {
                return false;
            }
        }

        if (alert.getMinimumExperience() != null) {
            if (job.getExperienceMax() == null || job.getExperienceMax() < alert.getMinimumExperience()) {
                return false;
            }
        }

        if (alert.getSkills() != null && !alert.getSkills().trim().isEmpty()) {
            String[] alertSkills = alert.getSkills().toLowerCase().split(",");
            String jobSkills = job.getSkills() != null ? job.getSkills().toLowerCase() : "";
            boolean matchesSkill = Arrays.stream(alertSkills)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .anyMatch(skill -> jobSkills.contains(skill));
            if (!matchesSkill) return false;
        }

        return true;
    }

    private void createAlertNotification(User candidate, Job job, JobAlert alert) {
        boolean alreadyNotified = notificationService.existsNotificationForJobAndAlert(
                candidate, job.getId(), alert.getId());
        if (alreadyNotified) {
            return;
        }

        String title = "New job matching your alert: " + alert.getName();
        String message = String.format("A new job \"%s\" at %s matches your alert criteria.",
                job.getTitle(), job.getLocation() != null ? job.getLocation() : "Unknown location");

        notificationService.createNotification(
                candidate,
                NotificationType.JOB_ALERT_MATCH,
                title,
                message,
                job.getId(),
                "JOB"
        );
    }

    private JobAlert getAlertIfExists(User candidate, Long alertId) {
        return jobAlertRepository.findById(alertId)
                .filter(a -> a.getCandidate().getId().equals(candidate.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Job Alert", "id", alertId));
    }

    private boolean hasAtLeastOneCriterion(CreateJobAlertRequest request) {
        return (request.getKeywords() != null && !request.getKeywords().trim().isEmpty()) ||
               (request.getLocation() != null && !request.getLocation().trim().isEmpty()) ||
               request.getWorkplaceType() != null ||
               request.getEmploymentType() != null ||
               request.getMinimumExperience() != null ||
               (request.getSkills() != null && !request.getSkills().trim().isEmpty());
    }

    private JobAlertResponse mapToResponse(JobAlert alert) {
        return JobAlertResponse.builder()
                .id(alert.getId())
                .name(alert.getName())
                .keywords(alert.getKeywords())
                .location(alert.getLocation())
                .workplaceType(alert.getWorkplaceType())
                .employmentType(alert.getEmploymentType())
                .minimumExperience(alert.getMinimumExperience())
                .skills(alert.getSkills())
                .active(alert.getActive())
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .lastTriggeredAt(alert.getLastTriggeredAt())
                .build();
    }
}
