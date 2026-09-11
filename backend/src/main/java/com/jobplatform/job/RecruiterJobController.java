package com.jobplatform.job;

import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.job.dto.CreateJobRequest;
import com.jobplatform.job.dto.JobResponse;
import com.jobplatform.job.dto.JobSummaryResponse;
import com.jobplatform.job.dto.RecruiterJobStats;
import com.jobplatform.job.dto.UpdateJobRequest;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.user.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recruiter/jobs")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterJobController {

    private final JobService jobService;

    public RecruiterJobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody CreateJobRequest request) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        JobResponse response = jobService.createJob(recruiter, request);
        return ResponseEntity.ok(ApiResponse.success("Job created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<JobSummaryResponse>>> getMyJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) JobStatus status) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        PagedResponse<JobSummaryResponse> response = jobService.getRecruiterJobs(recruiter, page, size, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long id) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        JobResponse response = jobService.getRecruiterJobById(recruiter, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobRequest request) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        JobResponse response = jobService.updateJob(recruiter, id, request);
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", response));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<JobResponse>> publishJob(@PathVariable Long id) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        JobResponse response = jobService.publishJob(recruiter, id);
        return ResponseEntity.ok(ApiResponse.success("Job published successfully", response));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<JobResponse>> closeJob(@PathVariable Long id) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        JobResponse response = jobService.closeJob(recruiter, id);
        return ResponseEntity.ok(ApiResponse.success("Job closed successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long id) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        jobService.deleteJob(recruiter, id);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<RecruiterJobStats>> getStats() {
        User recruiter = CurrentUserUtil.getCurrentUser();
        RecruiterJobStats stats = jobService.getRecruiterStats(recruiter);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
