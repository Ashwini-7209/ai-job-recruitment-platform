package com.jobplatform.jobalert;

import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.jobalert.dto.CreateJobAlertRequest;
import com.jobplatform.jobalert.dto.JobAlertResponse;
import com.jobplatform.jobalert.dto.UpdateJobAlertRequest;
import com.jobplatform.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/candidates/me/job-alerts")
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateJobAlertController {

    private final JobAlertService jobAlertService;

    public CandidateJobAlertController(JobAlertService jobAlertService) {
        this.jobAlertService = jobAlertService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobAlertResponse>> createAlert(
            @Valid @RequestBody CreateJobAlertRequest request) {
        User candidate = CurrentUserUtil.getCurrentUser();
        JobAlertResponse response = jobAlertService.createAlert(candidate, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job alert created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<JobAlertResponse>>> getAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User candidate = CurrentUserUtil.getCurrentUser();
        PagedResponse<JobAlertResponse> response = jobAlertService.getAlerts(candidate, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{alertId}")
    public ResponseEntity<ApiResponse<JobAlertResponse>> getAlert(@PathVariable Long alertId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        JobAlertResponse response = jobAlertService.getAlert(candidate, alertId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{alertId}")
    public ResponseEntity<ApiResponse<JobAlertResponse>> updateAlert(
            @PathVariable Long alertId,
            @Valid @RequestBody UpdateJobAlertRequest request) {
        User candidate = CurrentUserUtil.getCurrentUser();
        JobAlertResponse response = jobAlertService.updateAlert(candidate, alertId, request);
        return ResponseEntity.ok(ApiResponse.success("Job alert updated successfully", response));
    }

    @DeleteMapping("/{alertId}")
    public ResponseEntity<ApiResponse<Void>> deleteAlert(@PathVariable Long alertId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        jobAlertService.deleteAlert(candidate, alertId);
        return ResponseEntity.ok(ApiResponse.success("Job alert deleted successfully", null));
    }

    @PatchMapping("/{alertId}/status")
    public ResponseEntity<ApiResponse<JobAlertResponse>> toggleAlertStatus(@PathVariable Long alertId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        JobAlertResponse response = jobAlertService.toggleAlertStatus(candidate, alertId);
        return ResponseEntity.ok(ApiResponse.success("Job alert status updated", response));
    }
}
