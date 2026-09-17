package com.jobplatform.job;

import com.jobplatform.application.ApplicationRepository;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.job.dto.JobResponse;
import com.jobplatform.job.dto.JobSummaryResponse;
import com.jobplatform.job.dto.JobSummaryWithSavedResponse;
import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.WorkplaceType;
import com.jobplatform.savedjob.SavedJobRepository;
import com.jobplatform.user.User;
import com.jobplatform.user.UserRole;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/candidate/jobs")
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateJobController {

    private final JobService jobService;
    private final SavedJobRepository savedJobRepository;
    private final ApplicationRepository applicationRepository;

    public CandidateJobController(JobService jobService, SavedJobRepository savedJobRepository,
                                   ApplicationRepository applicationRepository) {
        this.jobService = jobService;
        this.savedJobRepository = savedJobRepository;
        this.applicationRepository = applicationRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<JobSummaryWithSavedResponse>>> getJobs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) WorkplaceType workplaceType,
            @RequestParam(required = false) Integer experienceMin,
            @RequestParam(required = false) Integer experienceMax,
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime postedAfter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {

        User candidate = CurrentUserUtil.getCurrentUser();

        PagedResponse<JobSummaryResponse> baseResponse = jobService.searchPublishedJobsCombined(
                q, location, employmentType, workplaceType,
                experienceMin, experienceMax, salaryMin, salaryMax,
                skills, companyName, postedAfter,
                page, size, sort);

        List<Long> jobIds = baseResponse.getContent().stream()
                .map(JobSummaryResponse::getJobId)
                .toList();

        Set<Long> savedJobIds = Collections.emptySet();
        Set<Long> appliedJobIds = Collections.emptySet();

        if (!jobIds.isEmpty()) {
            savedJobIds = savedJobRepository.findJobIdsByCandidateAndJobIds(candidate, jobIds)
                    .stream().collect(Collectors.toSet());
            appliedJobIds = applicationRepository.findJobIdsByCandidateAndJobIds(candidate, jobIds)
                    .stream().collect(Collectors.toSet());
        }

        Set<Long> finalSavedJobIds = savedJobIds;
        Set<Long> finalAppliedJobIds = appliedJobIds;

        List<JobSummaryWithSavedResponse> enrichedContent = baseResponse.getContent().stream()
                .map(base -> {
                    JobSummaryWithSavedResponse enriched = JobSummaryWithSavedResponse.from(base);
                    enriched.setSaved(finalSavedJobIds.contains(base.getJobId()));
                    enriched.setApplied(finalAppliedJobIds.contains(base.getJobId()));
                    return enriched;
                })
                .toList();

        PagedResponse<JobSummaryWithSavedResponse> response = PagedResponse.<JobSummaryWithSavedResponse>builder()
                .content(enrichedContent)
                .page(baseResponse.getPage())
                .size(baseResponse.getSize())
                .totalElements(baseResponse.getTotalElements())
                .totalPages(baseResponse.getTotalPages())
                .last(baseResponse.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobSummaryWithSavedResponse>> getJobById(@PathVariable Long id) {
        User candidate = CurrentUserUtil.getCurrentUser();
        JobResponse baseResponse = jobService.getPublishedJobById(id);
        Job job = jobService.getJobEntityById(id);

        boolean saved = savedJobRepository.existsByCandidateAndJob(candidate, job);
        boolean applied = applicationRepository.existsByCandidateAndJob(candidate, job);

        JobSummaryWithSavedResponse response = new JobSummaryWithSavedResponse();
        response.setId(baseResponse.getId());
        response.setJobId(baseResponse.getId());
        response.setTitle(baseResponse.getTitle());
        response.setDescription(baseResponse.getDescription());
        response.setRecruiterName(baseResponse.getRecruiterName());
        response.setLocation(baseResponse.getLocation());
        response.setEmploymentType(baseResponse.getEmploymentType());
        response.setWorkplaceType(baseResponse.getWorkplaceType());
        response.setExperienceMin(baseResponse.getExperienceMin());
        response.setExperienceMax(baseResponse.getExperienceMax());
        response.setSalaryMin(baseResponse.getSalaryMin());
        response.setSalaryMax(baseResponse.getSalaryMax());
        response.setSkills(baseResponse.getSkills());
        response.setStatus(baseResponse.getStatus());
        response.setPublishedAt(baseResponse.getPublishedAt());
        response.setApplicationDeadline(baseResponse.getApplicationDeadline());
        response.setCreatedAt(baseResponse.getCreatedAt());
        response.setSaved(saved);
        response.setApplied(applied);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
