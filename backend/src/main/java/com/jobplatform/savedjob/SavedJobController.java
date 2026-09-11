package com.jobplatform.savedjob;

import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.job.enums.EmploymentType;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.job.enums.WorkplaceType;
import com.jobplatform.savedjob.dto.SavedJobResponse;
import com.jobplatform.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidates/me/saved-jobs")
@PreAuthorize("hasRole('CANDIDATE')")
public class SavedJobController {

    private final SavedJobService savedJobService;

    public SavedJobController(SavedJobService savedJobService) {
        this.savedJobService = savedJobService;
    }

    @PostMapping("/{jobId}")
    public ResponseEntity<ApiResponse<SavedJobResponse>> saveJob(@PathVariable Long jobId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        SavedJobResponse response = savedJobService.saveJob(candidate, jobId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job saved successfully", response));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<ApiResponse<Void>> unsaveJob(@PathVariable Long jobId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        savedJobService.unsaveJob(candidate, jobId);
        return ResponseEntity.ok(ApiResponse.success("Job removed from saved list", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SavedJobResponse>>> getSavedJobs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) WorkplaceType workplaceType,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) JobStatus jobStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User candidate = CurrentUserUtil.getCurrentUser();
        PagedResponse<SavedJobResponse> response = savedJobService.getSavedJobs(
                candidate, q, location, workplaceType, employmentType, jobStatus, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{jobId}/status")
    public ResponseEntity<ApiResponse<Boolean>> getSavedStatus(@PathVariable Long jobId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        boolean saved = savedJobService.isJobSaved(candidate, jobId);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }
}
