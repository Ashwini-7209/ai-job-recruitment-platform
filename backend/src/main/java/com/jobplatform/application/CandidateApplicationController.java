package com.jobplatform.application;

import com.jobplatform.application.dto.ApplicationResponse;
import com.jobplatform.application.dto.ApplicationSummaryResponse;
import com.jobplatform.application.dto.CandidateApplicationDetailResponse;
import com.jobplatform.application.dto.CandidateApplicationStatsResponse;
import com.jobplatform.application.dto.CandidateApplicationSummaryResponse;
import com.jobplatform.application.dto.CreateApplicationRequest;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
import com.jobplatform.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateApplicationController {

    private final ApplicationService applicationService;

    public CandidateApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/jobs/{jobId}/applications")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyToJob(
            @PathVariable Long jobId,
            @Valid @RequestBody CreateApplicationRequest request) {
        User candidate = CurrentUserUtil.getCurrentUser();
        ApplicationResponse response = applicationService.applyToJob(candidate, jobId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", response));
    }

    @GetMapping("/candidates/me/applications")
    public ResponseEntity<ApiResponse<PagedResponse<CandidateApplicationSummaryResponse>>> getMyApplications(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        User candidate = CurrentUserUtil.getCurrentUser();
        PagedResponse<CandidateApplicationSummaryResponse> response =
                applicationService.searchCandidateApplications(candidate, q, status, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/candidates/me/applications/{applicationId}")
    public ResponseEntity<ApiResponse<CandidateApplicationDetailResponse>> getApplicationDetail(
            @PathVariable Long applicationId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        CandidateApplicationDetailResponse response = applicationService.getCandidateApplicationDetail(candidate, applicationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/candidates/me/applications/{applicationId}/withdraw")
    public ResponseEntity<ApiResponse<ApplicationResponse>> withdrawApplication(
            @PathVariable Long applicationId) {
        User candidate = CurrentUserUtil.getCurrentUser();
        ApplicationResponse response = applicationService.withdrawApplication(candidate, applicationId);
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn successfully", response));
    }

    @GetMapping("/candidates/me/application-stats")
    public ResponseEntity<ApiResponse<CandidateApplicationStatsResponse>> getMyStats() {
        User candidate = CurrentUserUtil.getCurrentUser();
        CandidateApplicationStatsResponse response = applicationService.getCandidateApplicationStats(candidate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
