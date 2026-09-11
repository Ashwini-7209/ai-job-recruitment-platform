package com.jobplatform.savedjob;

import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.exception.BadRequestException;
import com.jobplatform.exception.ResourceNotFoundException;
import com.jobplatform.job.Job;
import com.jobplatform.job.JobRepository;
import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.job.enums.WorkplaceType;
import com.jobplatform.savedjob.dto.SavedJobResponse;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SavedJobService {

    private static final Logger log = LoggerFactory.getLogger(SavedJobService.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public SavedJobService(SavedJobRepository savedJobRepository, JobRepository jobRepository,
                           ApplicationRepository applicationRepository) {
        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public SavedJobResponse saveJob(User candidate, Long jobId) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can save jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (savedJobRepository.existsByCandidateAndJob(candidate, job)) {
            throw new BadRequestException("Job is already saved");
        }

        SavedJob savedJob = SavedJob.builder()
                .candidate(candidate)
                .job(job)
                .build();

        SavedJob saved = savedJobRepository.save(savedJob);
        log.info("Job saved: candidate={} job={}", candidate.getEmail(), job.getTitle());
        return mapToResponse(saved, false);
    }

    @Transactional
    public void unsaveJob(User candidate, Long jobId) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can manage saved jobs");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        savedJobRepository.deleteByCandidateAndJob(candidate, job);
        log.info("Job unsaved: candidate={} job={}", candidate.getEmail(), job.getTitle());
    }

    @Transactional(readOnly = true)
    public PagedResponse<SavedJobResponse> getSavedJobs(User candidate, String query,
            String location, WorkplaceType workplaceType, EmploymentType employmentType,
            JobStatus jobStatus, int page, int size) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            throw new BadRequestException("Only candidates can view saved jobs");
        }

        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<SavedJob> savedJobs;
        boolean hasFilters = (query != null && !query.trim().isEmpty()) ||
                location != null || workplaceType != null || employmentType != null || jobStatus != null;

        if (hasFilters) {
            savedJobs = savedJobRepository.searchSavedJobsCombined(
                    candidate, query, location, workplaceType, employmentType, jobStatus, pageable);
        } else {
            savedJobs = savedJobRepository.findByCandidateOrderByCreatedAtDesc(candidate, pageable);
        }

        List<Long> jobIds = savedJobs.getContent().stream()
                .map(sj -> sj.getJob().getId())
                .toList();

        Set<Long> appliedJobIds = jobIds.isEmpty() ? Collections.emptySet() :
                applicationRepository.findJobIdsByCandidateAndJobIds(candidate, jobIds)
                        .stream().collect(Collectors.toSet());

        List<SavedJobResponse> responses = savedJobs.getContent().stream()
                .map(sj -> mapToResponse(sj, appliedJobIds.contains(sj.getJob().getId())))
                .toList();

        return PagedResponse.<SavedJobResponse>builder()
                .content(responses)
                .page(savedJobs.getNumber())
                .size(savedJobs.getSize())
                .totalElements(savedJobs.getTotalElements())
                .totalPages(savedJobs.getTotalPages())
                .last(savedJobs.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isJobSaved(User candidate, Long jobId) {
        if (candidate.getRole() != UserRole.CANDIDATE) {
            return false;
        }
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return false;
        }
        return savedJobRepository.existsByCandidateAndJob(candidate, job);
    }

    @Transactional(readOnly = true)
    public long getSavedCount(User candidate) {
        return savedJobRepository.countByCandidate(candidate);
    }

    private SavedJobResponse mapToResponse(SavedJob savedJob, boolean applied) {
        Job job = savedJob.getJob();

        return SavedJobResponse.builder()
                .savedJobId(savedJob.getId())
                .jobId(job.getId())
                .jobTitle(job.getTitle())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .workplaceType(job.getWorkplaceType())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .skills(job.getSkills())
                .jobStatus(job.getStatus())
                .deadline(job.getApplicationDeadline())
                .savedAt(savedJob.getCreatedAt())
                .applied(applied)
                .build();
    }
}
