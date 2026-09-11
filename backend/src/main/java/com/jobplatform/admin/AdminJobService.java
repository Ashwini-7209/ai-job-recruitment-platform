package com.jobplatform.admin;

import com.jobplatform.admin.audit.AuditLog;
import com.jobplatform.admin.audit.AuditLogRepository;
import com.jobplatform.admin.dto.AdminJobResponse;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminJobService {

    private static final Logger log = LoggerFactory.getLogger(AdminJobService.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final JobRepository jobRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminJobService(JobRepository jobRepository, AuditLogRepository auditLogRepository) {
        this.jobRepository = jobRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminJobResponse> getJobs(String query, JobStatus status, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Job> jobs;

        if (query != null && !query.trim().isEmpty()) {
            jobs = jobRepository.findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(
                    query.trim(), query.trim(), pageable);
        } else if (status != null) {
            jobs = jobRepository.findByStatus(status, pageable);
        } else {
            jobs = jobRepository.findAll(pageable);
        }

        return PagedResponse.<AdminJobResponse>builder()
                .content(jobs.getContent().stream().map(this::mapToResponse).toList())
                .page(jobs.getNumber())
                .size(jobs.getSize())
                .totalElements(jobs.getTotalElements())
                .totalPages(jobs.getTotalPages())
                .last(jobs.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminJobResponse getJob(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
        return mapToResponse(job);
    }

    @Transactional
    public AdminJobResponse updateJobStatus(User admin, Long jobId, JobStatus status) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        JobStatus oldStatus = job.getStatus();
        job.setStatus(status);
        Job updated = jobRepository.save(job);

        auditLogRepository.save(AuditLog.builder()
                .actor(admin)
                .action("JOB_STATUS_CHANGED")
                .entityType("JOB")
                .entityId(jobId)
                .description("Job \"" + job.getTitle() + "\" status changed from " + oldStatus + " to " + status)
                .build());

        log.info("Job status updated by admin {}: jobId={} status={}", admin.getEmail(), jobId, status);
        return mapToResponse(updated);
    }

    private AdminJobResponse mapToResponse(Job job) {
        return AdminJobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .recruiterName(job.getRecruiter().getFullName())
                .recruiterEmail(job.getRecruiter().getEmail())
                .location(job.getLocation())
                .workplaceType(job.getWorkplaceType())
                .employmentType(job.getEmploymentType())
                .status(job.getStatus())
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .createdAt(job.getCreatedAt())
                .publishedAt(job.getPublishedAt())
                .applicationDeadline(job.getApplicationDeadline())
                .build();
    }
}
