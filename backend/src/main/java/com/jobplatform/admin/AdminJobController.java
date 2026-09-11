package com.jobplatform.admin;

import com.jobplatform.admin.dto.AdminJobResponse;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.job.enums.JobStatus;
import com.jobplatform.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/jobs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminJobController {

    private final AdminJobService adminJobService;

    public AdminJobController(AdminJobService adminJobService) {
        this.adminJobService = adminJobService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AdminJobResponse>>> getJobs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<AdminJobResponse> response = adminJobService.getJobs(q, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<AdminJobResponse>> getJob(@PathVariable Long jobId) {
        AdminJobResponse response = adminJobService.getJob(jobId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{jobId}/status")
    public ResponseEntity<ApiResponse<AdminJobResponse>> updateJobStatus(
            @PathVariable Long jobId,
            @RequestParam JobStatus status) {
        User admin = CurrentUserUtil.getCurrentUser();
        AdminJobResponse response = adminJobService.updateJobStatus(admin, jobId, status);
        return ResponseEntity.ok(ApiResponse.success("Job status updated", response));
    }
}
