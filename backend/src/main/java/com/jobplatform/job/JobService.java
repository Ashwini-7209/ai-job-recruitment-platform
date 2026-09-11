package com.jobplatform.job;

import com.jobplatform.common.PagedResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.job.dto.CreateJobRequest;
import com.jobplatform.job.dto.JobResponse;
import com.jobplatform.job.dto.JobSummaryResponse;
import com.jobplatform.job.dto.RecruiterJobStats;
import com.jobplatform.job.dto.UpdateJobRequest;
import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.job.enums.WorkplaceType;
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

@Service
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public JobResponse createJob(User recruiter, CreateJobRequest request) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can create jobs");
        }
        validateJobData(request);

        Job job = Job.builder()
                .recruiter(recruiter)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .location(request.getLocation() != null ? request.getLocation().trim() : null)
                .employmentType(request.getEmploymentType())
                .workplaceType(request.getWorkplaceType())
                .experienceMin(request.getExperienceMin())
                .experienceMax(request.getExperienceMax())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .skills(request.getSkills() != null ? request.getSkills().trim() : null)
                .status(JobStatus.DRAFT)
                .applicationDeadline(request.getApplicationDeadline())
                .build();

        Job saved = jobRepository.save(job);
        log.info("Job created by recruiter {}: {}", recruiter.getEmail(), saved.getTitle());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<JobSummaryResponse> getRecruiterJobs(User recruiter, int page, int size, JobStatus status) {
        if (recruiter.getRole() != UserRole.RECRUITER) {
            throw new BadRequestException("Only recruiters can view recruiter jobs");
        }
        Pageable pageable = PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Job> jobs;
        if (status != null) {
            jobs = jobRepository.findByRecruiterAndStatus(recruiter, status, pageable);
        } else {
            jobs = jobRepository.findByRecruiter(recruiter, pageable);
        }

        return mapToPagedResponse(jobs);
    }

    @Transactional(readOnly = true)
    public JobResponse getRecruiterJobById(User recruiter, Long jobId) {
        Job job = getJobById(jobId);
        verifyRecruiterOwnership(job, recruiter);
        return mapToResponse(job);
    }

    @Transactional
    public JobResponse updateJob(User recruiter, Long jobId, UpdateJobRequest request) {
        Job job = getJobById(jobId);
        verifyRecruiterOwnership(job, recruiter);

        if (request.getTitle() != null) job.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) job.setDescription(request.getDescription().trim());
        if (request.getLocation() != null) job.setLocation(request.getLocation().trim());
        if (request.getEmploymentType() != null) job.setEmploymentType(request.getEmploymentType());
        if (request.getWorkplaceType() != null) job.setWorkplaceType(request.getWorkplaceType());
        if (request.getExperienceMin() != null) job.setExperienceMin(request.getExperienceMin());
        if (request.getExperienceMax() != null) job.setExperienceMax(request.getExperienceMax());
        if (request.getSalaryMin() != null) job.setSalaryMin(request.getSalaryMin());
        if (request.getSalaryMax() != null) job.setSalaryMax(request.getSalaryMax());
        if (request.getSkills() != null) job.setSkills(request.getSkills().trim());
        if (request.getApplicationDeadline() != null) job.setApplicationDeadline(request.getApplicationDeadline());

        validateJobRanges(job);

        Job saved = jobRepository.save(job);
        log.info("Job updated by recruiter {}: {}", recruiter.getEmail(), saved.getTitle());
        return mapToResponse(saved);
    }

    @Transactional
    public JobResponse publishJob(User recruiter, Long jobId) {
        Job job = getJobById(jobId);
        verifyRecruiterOwnership(job, recruiter);

        if (job.getStatus() != JobStatus.DRAFT) {
            throw new BadRequestException("Only draft jobs can be published");
        }
        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot publish a job with a past application deadline");
        }

        job.setStatus(JobStatus.PUBLISHED);
        job.setPublishedAt(LocalDateTime.now());

        Job saved = jobRepository.save(job);
        log.info("Job published by recruiter {}: {}", recruiter.getEmail(), saved.getTitle());
        return mapToResponse(saved);
    }

    @Transactional
    public JobResponse closeJob(User recruiter, Long jobId) {
        Job job = getJobById(jobId);
        verifyRecruiterOwnership(job, recruiter);

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new BadRequestException("Only published jobs can be closed");
        }

        job.setStatus(JobStatus.CLOSED);

        Job saved = jobRepository.save(job);
        log.info("Job closed by recruiter {}: {}", recruiter.getEmail(), saved.getTitle());
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteJob(User recruiter, Long jobId) {
        Job job = getJobById(jobId);
        verifyRecruiterOwnership(job, recruiter);

        if (job.getStatus() == JobStatus.PUBLISHED) {
            throw new BadRequestException("Cannot delete a published job. Close it first.");
        }

        jobRepository.delete(job);
        log.info("Job deleted by recruiter {}: {}", recruiter.getEmail(), job.getTitle());
    }

    @Transactional(readOnly = true)
    public RecruiterJobStats getRecruiterStats(User recruiter) {
        long total = jobRepository.countByRecruiter(recruiter);
        long draft = jobRepository.countByRecruiterAndStatus(recruiter, JobStatus.DRAFT);
        long published = jobRepository.countByRecruiterAndStatus(recruiter, JobStatus.PUBLISHED);
        long closed = jobRepository.countByRecruiterAndStatus(recruiter, JobStatus.CLOSED);

        return RecruiterJobStats.builder()
                .totalJobs(total)
                .draftJobs(draft)
                .publishedJobs(published)
                .closedJobs(closed)
                .build();
    }

    @Transactional(readOnly = true)
    public PagedResponse<JobSummaryResponse> getPublishedJobs(int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<Job> jobs = jobRepository.findByStatus(JobStatus.PUBLISHED, pageable);
        return mapToSummaryPagedResponse(jobs);
    }

    @Transactional(readOnly = true)
    public PagedResponse<JobSummaryResponse> searchPublishedJobs(String query, int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<Job> jobs = jobRepository.searchPublishedJobs(query, pageable);
        return mapToSummaryPagedResponse(jobs);
    }

    @Transactional(readOnly = true)
    public PagedResponse<JobSummaryResponse> filterPublishedJobs(
            String location, EmploymentType employmentType, WorkplaceType workplaceType,
            Integer experienceMin, Integer experienceMax, Integer salaryMin, Integer salaryMax,
            int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<Job> jobs = jobRepository.findPublishedJobsWithFilters(
                location, employmentType, workplaceType,
                experienceMin, experienceMax, salaryMin, salaryMax,
                pageable);
        return mapToSummaryPagedResponse(jobs);
    }

    @Transactional(readOnly = true)
    public PagedResponse<JobSummaryResponse> searchPublishedJobsCombined(
            String query, String location, EmploymentType employmentType, WorkplaceType workplaceType,
            Integer experienceMin, Integer experienceMax, Integer salaryMin, Integer salaryMax,
            int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        Page<Job> jobs = jobRepository.searchPublishedJobsCombined(
                query, location, employmentType, workplaceType,
                experienceMin, experienceMax, salaryMin, salaryMax,
                pageable);
        return mapToSummaryPagedResponse(jobs);
    }

    @Transactional(readOnly = true)
    public JobResponse getPublishedJobById(Long jobId) {
        Job job = getJobById(jobId);
        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Published job", "id", jobId);
        }
        return mapToResponse(job);
    }

    private Job getJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));
    }

    public Job getJobEntityById(Long jobId) {
        return getJobById(jobId);
    }

    private void verifyRecruiterOwnership(Job job, User recruiter) {
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new BadRequestException("You do not have permission to modify this job");
        }
    }

    private void validateJobData(CreateJobRequest request) {
        if (request.getExperienceMin() != null && request.getExperienceMax() != null) {
            if (request.getExperienceMin() < 0 || request.getExperienceMax() < 0) {
                throw new BadRequestException("Experience values must be non-negative");
            }
            if (request.getExperienceMin() > request.getExperienceMax()) {
                throw new BadRequestException("Minimum experience cannot exceed maximum experience");
            }
        }
        if (request.getSalaryMin() != null && request.getSalaryMax() != null) {
            if (request.getSalaryMin() < 0 || request.getSalaryMax() < 0) {
                throw new BadRequestException("Salary values must be non-negative");
            }
            if (request.getSalaryMin() > request.getSalaryMax()) {
                throw new BadRequestException("Minimum salary cannot exceed maximum salary");
            }
        }
        if (request.getApplicationDeadline() != null && request.getApplicationDeadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Application deadline must be in the future");
        }
    }

    private void validateJobRanges(Job job) {
        if (job.getExperienceMin() != null && job.getExperienceMax() != null) {
            if (job.getExperienceMin() < 0 || job.getExperienceMax() < 0) {
                throw new BadRequestException("Experience values must be non-negative");
            }
            if (job.getExperienceMin() > job.getExperienceMax()) {
                throw new BadRequestException("Minimum experience cannot exceed maximum experience");
            }
        }
        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            if (job.getSalaryMin() < 0 || job.getSalaryMax() < 0) {
                throw new BadRequestException("Salary values must be non-negative");
            }
            if (job.getSalaryMin() > job.getSalaryMax()) {
                throw new BadRequestException("Minimum salary cannot exceed maximum salary");
            }
        }
    }

    private Pageable buildPageable(int page, int size, String sort) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        Sort sortBy = switch (sort != null ? sort : "newest") {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "deadline" -> Sort.by(Sort.Direction.ASC, "applicationDeadline");
            case "salary_high" -> Sort.by(Sort.Direction.DESC, "salaryMax");
            case "salary_low" -> Sort.by(Sort.Direction.ASC, "salaryMin");
            case "relevance" -> Sort.by(Sort.Direction.DESC, "publishedAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        return PageRequest.of(safePage, safeSize, sortBy);
    }

    private JobResponse mapToResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .recruiterId(job.getRecruiter().getId())
                .recruiterName(job.getRecruiter().getFullName())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .workplaceType(job.getWorkplaceType())
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .skills(job.getSkills())
                .status(job.getStatus())
                .applicationDeadline(job.getApplicationDeadline())
                .publishedAt(job.getPublishedAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private JobSummaryResponse mapToSummaryResponse(Job job) {
        return JobSummaryResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .workplaceType(job.getWorkplaceType())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .skills(job.getSkills())
                .applicationDeadline(job.getApplicationDeadline())
                .createdAt(job.getCreatedAt())
                .build();
    }

    private PagedResponse<JobSummaryResponse> mapToSummaryPagedResponse(Page<Job> jobs) {
        return PagedResponse.<JobSummaryResponse>builder()
                .content(jobs.getContent().stream().map(this::mapToSummaryResponse).toList())
                .page(jobs.getNumber())
                .size(jobs.getSize())
                .totalElements(jobs.getTotalElements())
                .totalPages(jobs.getTotalPages())
                .last(jobs.isLast())
                .build();
    }

    private PagedResponse<JobSummaryResponse> mapToPagedResponse(Page<Job> jobs) {
        return PagedResponse.<JobSummaryResponse>builder()
                .content(jobs.getContent().stream().map(this::mapToSummaryResponse).toList())
                .page(jobs.getNumber())
                .size(jobs.getSize())
                .totalElements(jobs.getTotalElements())
                .totalPages(jobs.getTotalPages())
                .last(jobs.isLast())
                .build();
    }
}
