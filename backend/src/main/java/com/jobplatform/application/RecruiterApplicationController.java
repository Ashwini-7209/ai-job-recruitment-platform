package com.jobplatform.application;

import com.jobplatform.application.dto.ApplicationNoteRequest;
import com.jobplatform.application.dto.ApplicationNoteResponse;
import com.jobplatform.application.dto.ApplicationResponse;
import com.jobplatform.application.dto.ApplicationStats;
import com.jobplatform.application.dto.ApplicationSummaryResponse;
import com.jobplatform.application.dto.RecruiterAggregateStatsResponse;
import com.jobplatform.application.dto.RecruiterApplicationDetailResponse;
import com.jobplatform.application.dto.UpdateApplicationStatusRequest;
import com.jobplatform.application.enums.ApplicationStatus;
import com.jobplatform.common.ApiResponse;
import com.jobplatform.common.CurrentUserUtil;
import com.jobplatform.common.PagedResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recruiters/me")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationNoteService noteService;
    private final RecruiterApplicationDetailService detailService;

    public RecruiterApplicationController(
            ApplicationService applicationService,
            ApplicationNoteService noteService,
            RecruiterApplicationDetailService detailService) {
        this.applicationService = applicationService;
        this.noteService = noteService;
        this.detailService = detailService;
    }

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationSummaryResponse>>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) ApplicationStatus status) {
        User recruiter = CurrentUserUtil.getCurrentUser();

        boolean hasFilters = (q != null && !q.isBlank()) || jobId != null || status != null;

        PagedResponse<ApplicationSummaryResponse> response;
        if (hasFilters) {
            response = applicationService.searchRecruiterApplications(
                    recruiter, q, jobId, status, page, size, sort);
        } else {
            response = applicationService.getRecruiterApplications(recruiter, page, size, sort);
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/jobs/{jobId}/applications")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationSummaryResponse>>> getJobApplications(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        PagedResponse<ApplicationSummaryResponse> response = applicationService.getRecruiterJobApplications(recruiter, jobId, page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/jobs/{jobId}/applications/stats")
    public ResponseEntity<ApiResponse<ApplicationStats>> getJobApplicationStats(@PathVariable Long jobId) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        ApplicationStats stats = applicationService.getRecruiterJobStats(recruiter, jobId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ApiResponse<RecruiterApplicationDetailResponse>> getApplicationDetail(
            @PathVariable Long applicationId) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        RecruiterApplicationDetailResponse response = detailService.getApplicationDetail(recruiter, applicationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        ApplicationResponse response = applicationService.updateApplicationStatus(recruiter, applicationId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Application status updated successfully", response));
    }

    @GetMapping("/application-stats")
    public ResponseEntity<ApiResponse<RecruiterAggregateStatsResponse>> getAggregateStats() {
        User recruiter = CurrentUserUtil.getCurrentUser();
        RecruiterAggregateStatsResponse stats = applicationService.getRecruiterAggregateStats(recruiter);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/applications/{applicationId}/notes")
    public ResponseEntity<ApiResponse<ApplicationNoteResponse>> createNote(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationNoteRequest request) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        ApplicationNoteResponse response = noteService.createNote(recruiter, applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note created successfully", response));
    }

    @GetMapping("/applications/{applicationId}/notes")
    public ResponseEntity<ApiResponse<List<ApplicationNoteResponse>>> getNotes(
            @PathVariable Long applicationId) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        List<ApplicationNoteResponse> response = noteService.getNotes(recruiter, applicationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/applications/{applicationId}/notes/{noteId}")
    public ResponseEntity<ApiResponse<ApplicationNoteResponse>> updateNote(
            @PathVariable Long applicationId,
            @PathVariable Long noteId,
            @Valid @RequestBody ApplicationNoteRequest request) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        ApplicationNoteResponse response = noteService.updateNote(recruiter, applicationId, noteId, request);
        return ResponseEntity.ok(ApiResponse.success("Note updated successfully", response));
    }

    @DeleteMapping("/applications/{applicationId}/notes/{noteId}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable Long applicationId,
            @PathVariable Long noteId) {
        User recruiter = CurrentUserUtil.getCurrentUser();
        noteService.deleteNote(recruiter, applicationId, noteId);
        return ResponseEntity.ok(ApiResponse.success("Note deleted successfully", null));
    }
}
